/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.businessmatching.updateservice.add

import controllers.businessmatching.updateservice.AddBusinessTypeHelper
import generators.businessmatching.BusinessMatchingGenerator
import models.businessmatching._
import models.flowmanagement.{AddBusinessTypeFlowModel, TradingPremisesPageId}
import models.status.SubmissionDecisionApproved
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.businessmatching.BusinessMatchingService
import utils.{AuthorisedFixture, DependencyMocks, AmlsSpec}

class TradingPremisesControllerSpec extends AmlsSpec with BusinessMatchingGenerator {

  sealed trait Fixture extends AuthorisedFixture with DependencyMocks {
    self =>

    val request = addToken(authRequest)
    val mockBusinessMatchingService = mock[BusinessMatchingService]
    val mockUpdateServiceHelper = mock[AddBusinessTypeHelper]

    val controller = new TradingPremisesController(
      authConnector = self.authConnector,
      dataCacheConnector = mockCacheConnector,
      statusService = mockStatusService,
      businessMatchingService = mockBusinessMatchingService,
      helper = mockUpdateServiceHelper,
      router = createRouter[AddBusinessTypeFlowModel]
    )

    mockCacheFetch(Some(AddBusinessTypeFlowModel(Some(HighValueDealing))))
    mockApplicationStatus(SubmissionDecisionApproved)
  }

  "TradingPremisesController" when {

    "get is called" must {
      "return OK with trading_premises view" in new Fixture {
        val result = controller.get()(request)
        status(result) must be(OK)

        contentAsString(result) must include(
          Messages(
            "businessmatching.updateservice.tradingpremises.heading",
            Messages(s"businessmatching.registerservices.servicename.lbl.${BusinessActivities.getValue(HighValueDealing)}.phrased")
          ))
      }
    }

    "post is called" must {

      "with a valid request" must {
        "redirect" when {
          "request equals Yes" in new Fixture {

            mockCacheUpdate[AddBusinessTypeFlowModel](Some(AddBusinessTypeFlowModel.key), AddBusinessTypeFlowModel())

            val result = controller.post()(request.withFormUrlEncodedBody(
              "tradingPremisesNewActivities" -> "true"
            ))

            status(result) mustBe SEE_OTHER

            controller.router.verify(TradingPremisesPageId,
              AddBusinessTypeFlowModel(areNewActivitiesAtTradingPremises = Some(true), hasChanged = true))
          }
        }

        "when request equals No" when {
          "progress to the 'new service information' page" when {
            "an activity that generates a section has been chosen" in new Fixture {
              mockCacheUpdate[AddBusinessTypeFlowModel](Some(AddBusinessTypeFlowModel.key), AddBusinessTypeFlowModel(Some(HighValueDealing)))

              val result = controller.post()(request.withFormUrlEncodedBody(
                "tradingPremisesNewActivities" -> "false"
              ))

              status(result) mustBe SEE_OTHER

              controller.router.verify(TradingPremisesPageId,
                AddBusinessTypeFlowModel(Some(HighValueDealing), areNewActivitiesAtTradingPremises = Some(false), hasChanged = true))
            }
          }
        }
      }

      "on invalid request" must {
        "return badRequest" in new Fixture {
          val result = controller.post()(request)

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
}
