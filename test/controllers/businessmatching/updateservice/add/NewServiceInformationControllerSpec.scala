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

package controllers.businessmatching.updateservice.add

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import models.businessmatching._
import models.flowmanagement.{AddServiceFlowModel, NewServiceInformationPageId}
import org.jsoup.Jsoup
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import services.StatusService
import services.businessmatching.{BusinessMatchingService, NextService}
import services.flowmanagement.Router
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{AuthorisedFixture, DependencyMocks, FutureAssertions, GenericTestHelper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class NewServiceInformationControllerSpec extends GenericTestHelper with MockitoSugar with FutureAssertions with ScalaFutures {

  sealed trait Fixture extends AuthorisedFixture with DependencyMocks {
    self =>

    val request = addToken(authRequest)
    val mockBusinessMatchingService = mock[BusinessMatchingService]

    val controller = new NewServiceInformationController(
      self.authConnector,
      mockCacheConnector,
      mockBusinessMatchingService,
      messagesApi,
      createRouter[AddServiceFlowModel]
    )

    when {
      controller.businessMatchingService.getModel(any(),any(),any())
    } thenReturn OptionT.some[Future, BusinessMatching](BusinessMatching(
      activities = Some(BusinessActivities(Set(AccountancyServices)))
    ))

    when {
      controller.businessMatchingService.getSubmittedBusinessActivities(any(), any(), any())
    } thenReturn OptionT.some[Future, Set[BusinessActivity]](Set(AccountancyServices))

    val flowModel = AddServiceFlowModel(Some(AccountancyServices), Some(true))
    mockCacheFetch(Some(flowModel), Some(AddServiceFlowModel.key))
  }

  "NewServiceInformationController" when {

    "get is called" must {
      "return OK with new_service_information view" in new Fixture {
        val result = controller.get()(request)

        status(result) must be(OK)
        Jsoup.parse(contentAsString(result)).title() must include(Messages("businessmatching.updateservice.newserviceinformation.title"))
      }
    }
  }

  "post is called" must {

    "return OK with new_service_information view" in new Fixture {
      val result = controller.post()(request.withFormUrlEncodedBody(
        "addmoreactivities" -> "false"
      ))

      status(result) mustBe SEE_OTHER
      controller.router.verify(NewServiceInformationPageId, flowModel)
    }
  }
}
