/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
 * All modifications are copyright (c) 2020 Devside SRL.
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.verification.controller;


import app.coronawarn.verification.domain.VerificationAppSession;
import app.coronawarn.verification.exception.VerificationServerException;
import app.coronawarn.verification.model.AppSessionSourceOfTrust;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.LabTestResult;
import app.coronawarn.verification.model.RegistrationToken;
import app.coronawarn.verification.model.TestResult;
import app.coronawarn.verification.service.AppSessionService;
import app.coronawarn.verification.service.FakeDelayService;
import app.coronawarn.verification.service.FakeRequestService;
import app.coronawarn.verification.service.TestResultServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Optional;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * This class represents the rest controller for requests regarding test states.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/version/v1")
@Validated
@Profile("external")
public class ExternalTestStateController {

  /**
   * The route to the test status of the COVID-19 test endpoint.
   */
  public static final String TESTRESULT_ROUTE = "/testresult";

  public static final Integer RESPONSE_PADDING_LENGTH = 45;

  @NonNull
  private final FakeRequestService fakeRequestController;

  @NonNull
  private final AppSessionService appSessionService;

  @NonNull
  private final TestResultServerService testResultServerService;

  @NonNull
  private final FakeDelayService fakeDelayService;

  /**
   * Returns the test status of the COVID-19 test with cwa-fake header.
   *
   * @param registrationToken generated by a hashed guid {@link RegistrationToken}
   * @param fake flag for fake request
   * @return result of the test, which can be POSITIVE, NEGATIVE, INVALID, PENDING or FAILED will POSITIVE for TeleTan
   */
  @Operation(
    summary = "COVID-19 test result for given RegistrationToken",
    description = "Gets the result of COVID-19 Test. "
      + "If the RegistrationToken belongs to a TeleTan the result is always positive"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Testresult retrieved")})
  @PostMapping(value = TESTRESULT_ROUTE,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )

  // TODO: coronalert-app - here we are polling for a test state based on the registration token.
  // TODO: coronalert-app - Instead of registration token we will use an R1 + t0
  public DeferredResult<ResponseEntity<TestResult>> getTestState(
    @Valid @RequestBody RegistrationToken registrationToken,
    @RequestHeader(value = "cwa-fake", required = false) String fake) {
    if ((fake != null) && (fake.equals("1"))) {
      return fakeRequestController.getTestState();
    }
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Optional<VerificationAppSession> appSession =
      appSessionService.getAppSessionByToken(registrationToken.getRegistrationToken());
    if (appSession.isPresent()) {
      AppSessionSourceOfTrust sourceOfTrust = appSession.get().getSourceOfTrust();
      DeferredResult<ResponseEntity<TestResult>> deferredResult = new DeferredResult<>();

      switch (sourceOfTrust) {
        case HASHED_GUID:
          String hash = appSession.get().getHashedGuid();
          //// TODO: coronalert-app - here we need to retrieve the testresult based on R1 + t0
          TestResult testResult = testResultServerService.result(new HashedGuid(hash));
          testResult.setResponsePadding(RandomStringUtils.randomAlphanumeric(RESPONSE_PADDING_LENGTH));
          log.debug("Result {}",testResult);
          log.info("The result for registration token based on hashed Guid will be returned.");
          stopWatch.stop();
          fakeDelayService.updateFakeTestRequestDelay(stopWatch.getTotalTimeMillis());
          deferredResult.setResult(ResponseEntity.ok(testResult));
          return deferredResult;
        case TELETAN:
          log.info("The result for registration token based on teleTAN will be returned.");
          stopWatch.stop();
          fakeDelayService.updateFakeTestRequestDelay(stopWatch.getTotalTimeMillis());
          deferredResult.setResult(ResponseEntity.ok(
            new TestResult(LabTestResult.POSITIVE, TestResult.ResultChannel.LAB,
              RandomStringUtils.randomAlphanumeric(RESPONSE_PADDING_LENGTH))));
          return deferredResult;
        default:
          stopWatch.stop();
          throw new VerificationServerException(HttpStatus.BAD_REQUEST,
            "Unknown source of trust inside the appsession for the registration token");
      }
    }
    log.info("The registration token doesn't exists.");
    throw new VerificationServerException(HttpStatus.BAD_REQUEST,
      "Returning the test result for the registration token failed");
  }

}
