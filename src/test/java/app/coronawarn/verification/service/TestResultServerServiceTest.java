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

package app.coronawarn.verification.service;

import app.coronawarn.verification.client.TestResultServerClient;
import app.coronawarn.verification.model.HashedGuid;
import app.coronawarn.verification.model.LabTestResult;
import static app.coronawarn.verification.model.LabTestResult.POSITIVE;
import static app.coronawarn.verification.model.LabTestResult.REDEEMED;
import app.coronawarn.verification.model.MobileTestResultRequest;
import app.coronawarn.verification.model.TestResult;
import static app.coronawarn.verification.model.TestResult.ResultChannel.LAB;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.http.ResponseEntity;

public class TestResultServerServiceTest {

  public static final String TEST_GUI_HASH_1 = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
  public static final String TEST_GUI_HASH_2 = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13c";
  private static final String TEST_RESULT_PADDING = "";
  public static final TestResult TEST_LAB_POSITIVE_RESULT = new TestResult(POSITIVE, LAB);
  public static final TestResult TEST_LAB_REDEEMED_RESULT = new TestResult(REDEEMED, LAB);
  private TestResultServerService testResultServerService;

  @Before
  public void setUp() {
    testResultServerService = new TestResultServerService(new TestResultServerClientMock());
  }

  /**
   * Test result method by positive status.
   */
  @Test
  public void resultPositive() {
    TestResult testResult = testResultServerService.result(new HashedGuid(TEST_GUI_HASH_1));
    assertThat(testResult).isEqualTo(TEST_LAB_POSITIVE_RESULT);
  }

  /**
   * Test result method by redeemed status.
   */
  @Test
  public void resultRedeemed() {
    TestResult testResult = testResultServerService.result(new HashedGuid(TEST_GUI_HASH_2));
    assertThat(testResult).isEqualTo(TEST_LAB_REDEEMED_RESULT);
  }

  public static class TestResultServerClientMock implements TestResultServerClient {

    @Override
    public TestResult pollTestResult(MobileTestResultRequest mobileTestResultRequest) {
      return null;
    }

    @Override
    public ResponseEntity<Void> ackTestResult(MobileTestResultRequest mobileTestResultRequest) {
      return null;
    }

    @Override
    public TestResult result(HashedGuid guid) {
      if (guid.getId().equals(TEST_GUI_HASH_1)) {
        return new TestResult(POSITIVE, LAB);
      }
      return new TestResult(LabTestResult.REDEEMED, LAB);
    }
  }
}
