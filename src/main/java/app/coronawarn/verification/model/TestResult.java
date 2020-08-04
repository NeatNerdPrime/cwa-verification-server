/*
 * Corona-Warn-App / cwa-verification
 *
 * (C) 2020, T-Systems International GmbH
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

package app.coronawarn.verification.model;

import static app.coronawarn.verification.model.LabTestResult.PENDING;
import static app.coronawarn.verification.model.TestResult.ResultChannel.UNKNOWN;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;


/**
 * This class represents the TestResult.
 *
 * @see <a href="https://github.com/corona-warn-app/cwa-testresult-server/blob/master/docs/architecture-overview.md#core-entities">Core Entities</a>
 */
@Schema(
  description = "The test result model."
)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

  @NonNull
  private LabTestResult result;

  @NonNull
  private ResultChannel resultChannel;

  /**
   * Create a test result with a result,channel and padding.
   *
   * @param result the result.
   * @param resultChannel the channel.
   * @param responsePadding the padding.
   */
  public TestResult(LabTestResult result,ResultChannel resultChannel,String responsePadding) {
    this.result = result;
    this.resultChannel = resultChannel;
    this.responsePadding = responsePadding;
  }

  /**
   * Create a test result with a result,channel and padding.
   *
   * @param result the result.
   * @param resultChannel the channel.
   */
  public TestResult(LabTestResult result,ResultChannel resultChannel) {
    this(result,resultChannel,null);
  }

  /**
   * Create a pending result.
   *
   * @param mobileTestId The mobile test id.
   * @return
   */
  public static TestResult dummyTestResult() {
    return new TestResult()
      .setResult(PENDING)
      .setResultChannel(UNKNOWN)
      .setDateTestCommunicated(LocalDate.now())
      .setDatePatientInfectious(LocalDate.now())
      .setDateSampleCollected(LocalDate.now())
      .setDateTestPerformed(LocalDate.now());
  }

  private LocalDate datePatientInfectious;

  private LocalDate dateSampleCollected;

  private LocalDate dateTestPerformed;

  private LocalDate dateTestCommunicated;

  @Transient
  private String responsePadding;

  public enum ResultChannel {
    UNKNOWN,LAB,DOCTOR
  }

}
