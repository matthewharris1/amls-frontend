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

package controllers.renewal

import connectors.DataCacheConnector
import models.renewal.{CashPayments, CashPaymentsCustomerNotMet, HowCashPaymentsReceived, PaymentMethods, Renewal}
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.api.test.Helpers.{contentAsString, status}
import services.RenewalService
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AmlsSpec, AuthorisedFixture}

import scala.concurrent.Future

class HowCashPaymentsReceivedControllerSpec extends AmlsSpec {

  lazy val mockDataCacheConnector = mock[DataCacheConnector]
  lazy val mockRenewalService = mock[RenewalService]

  val receiveCashPayments = CashPayments(CashPaymentsCustomerNotMet(true), Some(HowCashPaymentsReceived(PaymentMethods(true,true,Some("other")))))
  val doNotreceiveCashPayments = CashPayments(CashPaymentsCustomerNotMet(false), None)

  trait Fixture extends AuthorisedFixture {
    self => val request = addToken(authRequest)

    val controller = new HowCashPaymentsReceivedController (
      dataCacheConnector = mockDataCacheConnector,
      authConnector = self.authConnector,
      renewalService = mockRenewalService
    )

    when(mockRenewalService.getRenewal(any(),any(),any()))
      .thenReturn(Future.successful(None))

    when(mockRenewalService.updateRenewal(any())(any(),any(),any()))
      .thenReturn(Future.successful(new CacheMap("", Map.empty)))
  }

  "HowCashPaymentsReceived" when {
    "get is called" must {
      "load the page" when {
        "renewal data is found for how cash payments received and pre-populate the data" in new Fixture {
          when(mockRenewalService.getRenewal(any(),any(),any()))
            .thenReturn(Future.successful(Some(Renewal(receiveCashPayments = Some(receiveCashPayments)))))

          val result = controller.get()(request)
          status(result) mustEqual OK

          val page = Jsoup.parse(contentAsString(result))

          page.select("input[type=checkbox][name=cashPaymentMethods.courier][value=true]").hasAttr("checked") must be(true)
          page.select("input[type=checkbox][name=cashPaymentMethods.direct][value=true]").hasAttr("checked") must be(true)
          page.select("input[type=checkbox][name=cashPaymentMethods.other][value=true]").hasAttr("checked") must be(true)
          page.select("input[type=text][name=cashPaymentMethods.details]").first().`val`() must be("other")
        }

        "no renewal data is found and show an empty form" in new Fixture {
          val result = controller.get()(request)
          status(result) mustEqual OK

          val page = Jsoup.parse(contentAsString(result))
          page.select("input[type=checkbox][name=cashPaymentMethods.courier][value=true]").hasAttr("checked") must be(false)
          page.select("input[type=checkbox][name=cashPaymentMethods.direct][value=true]").hasAttr("checked") must be(false)
          page.select("input[type=checkbox][name=cashPaymentMethods.other][value=true]").hasAttr("checked") must be(false)
          page.select("input[type=text][name=cashPaymentMethods.details]").first().`val`() must be("")
        }
      }
    }

    "post is called" must {
      "show a bad request with an invalid request" in new Fixture {

        val result = controller.post()(request)
        status(result) mustEqual BAD_REQUEST
      }

      "redirect to summary page if valid request" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody(
          "cashPaymentMethods.courier" -> "true",
          "cashPaymentMethods.direct" -> "true",
          "cashPaymentMethods.other" -> "true",
          "cashPaymentMethods.details" -> "other"
        )

        val result = controller.post()(newRequest)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SummaryController.get().url)
      }
    }
  }
}
