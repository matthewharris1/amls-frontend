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

package controllers

import connectors.{AmlsConnector, AuthenticatorConnector, FeeConnector}
import models.businesscustomer.{Address, ReviewDetails}
import models.businessmatching.{BusinessMatching, BusinessType}
import models.responsiblepeople.ResponsiblePeople
import models.status._
import models.{Country, FeeResponse}
import org.jsoup.Jsoup
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeApplication
import play.api.test.Helpers._
import services._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, DependencyMocks, GenericTestHelper}

import scala.concurrent.Future

class StatusControllerWithoutAmendmentsSpec extends GenericTestHelper with MockitoSugar {

  val cacheMap = mock[CacheMap]

  trait Fixture extends AuthorisedFixture with DependencyMocks {
    self => val request = addToken(authRequest)
    val controller = new StatusController (
      mock[LandingService],
      mock[StatusService],
      mock[AuthEnrolmentsService],
      mock[FeeConnector],
      mock[RenewalService],
      mock[ProgressService],
      mock[AmlsConnector],
      mockCacheConnector,
      mock[AuthenticatorConnector],
      self.authConnector
    )

    mockCacheFetch[Seq[ResponsiblePeople]](Some(Seq(ResponsiblePeople())), Some(ResponsiblePeople.key))
  }

  override lazy val app = FakeApplication(additionalConfiguration = Map("microservice.services.feature-toggle.amendments" -> false) )

  "StatusController" must {
    "hide amendment/variation link when amendments toggle is off" in new Fixture {

      val reviewDtls = ReviewDetails("BusinessName", Some(BusinessType.LimitedCompany),
        Address("line1", "line2", Some("line3"), Some("line4"), Some("AA11 1AA"), Country("United Kingdom", "GB")), "XE0000000000000")

      when(controller.landingService.cacheMap(any(), any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))

      when(cacheMap.getEntry[BusinessMatching](contains(BusinessMatching.key))(any()))
        .thenReturn(Some(BusinessMatching(Some(reviewDtls), None)))

      when(controller.enrolmentsService.amlsRegistrationNumber(any(), any(), any()))
        .thenReturn(Future.successful(Some("XAML00000000000")))

      when(controller.statusService.getDetailedStatus(any(), any(), any()))
        .thenReturn(Future.successful(SubmissionReadyForReview, None))

      when(controller.feeConnector.feeResponse(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(mock[FeeResponse]))

      when(controller.amlsConnector.getPaymentByAmlsReference(any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when {
        controller.dataCache.fetch[BusinessMatching](eqTo(BusinessMatching.key))(any(), any(), any())
      } thenReturn Future.successful(Some(BusinessMatching(Some(reviewDtls), None)))

      val result = controller.get()(request)
      status(result) must be(OK)

      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByClass("statusblock").html() must include(Messages("status.amendment.edit"))
    }
  }
}
