/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package typeclasses.confirmation

import connectors.DataCacheConnector
import generators.{AmlsReferenceNumberGenerator, ResponsiblePersonGenerator}
import models.businessmatching.{BusinessActivities, BusinessActivity}
import models.responsiblepeople.ResponsiblePerson
import models.{SubmissionResponse, SubscriptionFees, SubscriptionResponse}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import services.ConfirmationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext

/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class ResponsiblePeopleRowsPhase2Spec extends PlaySpec
  with MockitoSugar
  with ScalaFutures
  with IntegrationPatience
  with OneAppPerSuite
  with ResponsiblePersonGenerator
  with generators.tradingpremises.TradingPremisesGenerator
  with AmlsReferenceNumberGenerator {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.feature-toggle.phase-2-changes" -> true)
    .build()

  trait Fixture {

    val TestConfirmationService = new ConfirmationService(
      mock[DataCacheConnector]
    )
  }
    implicit val authContext = mock[AuthContext]
    implicit val headerCarrier = HeaderCarrier()

    "responsible people rows with phase2 toggle" should {
      "return an approval check row" when {
        "The business is HVD, EAB or ASP and has answered no to both the approvals question and F&P question" in new Fixture {
          pending
        }
      }

      "not return an approval check row" when {
        "The business is MSB or TCSP" in new Fixture {
          pending
        }

        "The business isn't HVD or EAB or ASP" in new Fixture {
          pending
        }

        "The business has answered yes to Fit and Proper Question" in new Fixture {
          pending
        }

        "The business has answered yes to Approval Check Question" in new Fixture {
          pending
        }
      }
      "return a Fit and Proper row" when {
        "The business is MSB or TCSP along with HVD, EAB or ASP and hasn't passed F&P" in new Fixture {
          pending
        }

        "The business is MSB or TCSP only and hasn't passed F&P" in new Fixture {
          pending
        }
      }

      "Not return a Fit and Proper row" when {
        "The business has answered yes to Fit and Proper Question" in new Fixture {
          pending
        }
        "The business doesn't have any business activities" in new Fixture {
          pending
        }
        "The business is HVD, EAB or ASP and has answered no to both the approvals question and F&P question" in new Fixture {
          pending
        }
      }
    }
}
