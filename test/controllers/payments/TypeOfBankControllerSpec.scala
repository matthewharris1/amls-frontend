/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.payments

import connectors.PayApiConnector
import generators.AmlsReferenceNumberGenerator
import models.confirmation.Currency
import models.payments._
import models.status.SubmissionReadyForReview
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito.{verify, when}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.{AuthEnrolmentsService, PaymentsService, StatusService, SubmissionResponseService}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.{ExecutionContext, Future}

class TypeOfBankControllerSpec extends PlaySpec with GenericTestHelper{

  trait Fixture extends AuthorisedFixture { self =>

    val request = addToken(authRequest)

    implicit val hc: HeaderCarrier = new HeaderCarrier()
    implicit val ac: AuthContext = mock[AuthContext]
    implicit val ec: ExecutionContext = mock[ExecutionContext]

    val controller = new TypeOfBankController(
      authConnector = self.authConnector
    )

  }

  "TypeOfBankController" when {

    "get is called" must {
      "return OK with view" in new Fixture {

        val result = controller.get()(request)

        status(result) must be(OK)
        contentAsString(result) must include(Messages("payments.typeofbank.title"))

      }
    }

    "post is called" when {

      "form value is true" must {
        "redirect to BankDetails" in new Fixture {

          val postRequest = request.withFormUrlEncodedBody(
            "typeOfBank" -> "true"
          )

          val result = controller.post()(postRequest)

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be (Some(controllers.payments.routes.BankDetailsController.get(true).url))

        }
      }

      "form value is false" must {
        "redirect to BankDetails" in new Fixture {

          val postRequest = request.withFormUrlEncodedBody(
            "typeOfBank" -> "false"
          )

          val result = controller.post()(postRequest)
          val body = contentAsString(result)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) must be (Some(controllers.payments.routes.BankDetailsController.get(false).url))
        }
      }

      "request is invalid" must {
        "return BAD_REQUEST" in new Fixture {

          val postRequest = request.withFormUrlEncodedBody(
            "typeOfBank" -> "01"
          )

          val result = controller.post()(postRequest)
          status(result) mustBe BAD_REQUEST

        }
      }
    }

  }

}