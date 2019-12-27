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

package controllers.estateagentbusiness

import connectors.DataCacheConnector
import controllers.actions.SuccessfulAuthAction
import models.businessdetails.{ActivityStartDate, BusinessDetails}
import models.estateagentbusiness._
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AmlsSpec, AuthorisedFixture}

import scala.concurrent.Future

class ServicesDateOfChangeControllerSpec extends AmlsSpec with MockitoSugar  {

  trait Fixture {
    self => val request = addToken(authRequest)

    val controller = new ServicesDateOfChangeController (
      dataCacheConnector = mock[DataCacheConnector],
      SuccessfulAuthAction,
      ds = commonDependencies,
      cc = mockMcc)
  }

  val emptyCache = CacheMap("", Map.empty)

  "ServicesDateOfChangeController" must {

    "on get display date of change view" in new Fixture {
      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include(Messages("summary.estateagentbusiness"))
    }

    "submit with valid data and redirect to CYA for non residential" in new Fixture {

      val newRequest = requestWithUrlEncodedBody(
        "dateOfChange.day" -> "24",
        "dateOfChange.month" -> "2",
        "dateOfChange.year" -> "1990"
      )

      val eab = EstateAgentBusiness(Some(Services(Set(Commercial),None)),None,None)

      val mockCacheMap = mock[CacheMap]
      when(mockCacheMap.getEntry[BusinessDetails](BusinessDetails.key))
        .thenReturn(Some(BusinessDetails(activityStartDate = Some(ActivityStartDate(new LocalDate(1990, 2, 22))))))

      when(mockCacheMap.getEntry[EstateAgentBusiness](EstateAgentBusiness.key))
        .thenReturn(Some(eab))

      when(controller.dataCacheConnector.fetchAll(any())( any()))
        .thenReturn(Future.successful(Some(mockCacheMap)))

      when(controller.dataCacheConnector.save[EstateAgentBusiness](any(), any(), any())
        ( any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.estateagentbusiness.routes.SummaryController.get().url))
    }

    "submit and redirect to redress scheme for residential" in new Fixture {

      val newRequest = requestWithUrlEncodedBody(
        "dateOfChange.day" -> "24",
        "dateOfChange.month" -> "2",
        "dateOfChange.year" -> "1990"
      )

      val eab = EstateAgentBusiness(Some(Services(Set(Residential),None)),None,None)

      val mockCacheMap = mock[CacheMap]
      when(mockCacheMap.getEntry[BusinessDetails](BusinessDetails.key))
        .thenReturn(Some(BusinessDetails(activityStartDate = Some(ActivityStartDate(new LocalDate(1990, 2, 22))))))

      when(mockCacheMap.getEntry[EstateAgentBusiness](EstateAgentBusiness.key))
        .thenReturn(Some(eab))

      when(controller.dataCacheConnector.fetchAll(any())( any()))
        .thenReturn(Future.successful(Some(mockCacheMap)))

      when(controller.dataCacheConnector.save[EstateAgentBusiness](any(), any(), any())
        ( any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.estateagentbusiness.routes.ResidentialRedressSchemeController.get(true).url))
    }

    "fail submission when invalid date is supplied" in new Fixture {

      val newRequest = requestWithUrlEncodedBody(
        "dateOfChange.day" -> "24",
        "dateOfChange.month" -> "2",
        "dateOfChange.year" -> "199000"
      )

      val mockCacheMap = mock[CacheMap]
      when(mockCacheMap.getEntry[BusinessDetails](BusinessDetails.key))
        .thenReturn(Some(BusinessDetails(activityStartDate = Some(ActivityStartDate(new LocalDate(1990, 2, 24))))))

      when(mockCacheMap.getEntry[EstateAgentBusiness](EstateAgentBusiness.key))
        .thenReturn(None)

      when(controller.dataCacheConnector.fetchAll(any())( any()))
        .thenReturn(Future.successful(Some(mockCacheMap)))

      when(controller.dataCacheConnector.save[EstateAgentBusiness](any(),any(), any())
        ( any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(Messages("error.expected.jodadate.format"))
    }

    "fail submission when input date is before activity start date" in new Fixture {

      val newRequest = requestWithUrlEncodedBody(
        "dateOfChange.day" -> "24",
        "dateOfChange.month" -> "2",
        "dateOfChange.year" -> "1980"
      )

      val mockCacheMap = mock[CacheMap]
      when(mockCacheMap.getEntry[BusinessDetails](BusinessDetails.key))
        .thenReturn(Some(BusinessDetails(activityStartDate = Some(ActivityStartDate(new LocalDate(1990, 2, 24))))))

      when(mockCacheMap.getEntry[EstateAgentBusiness](EstateAgentBusiness.key))
        .thenReturn(Some(EstateAgentBusiness()))

      when(controller.dataCacheConnector.fetchAll(any())( any()))
        .thenReturn(Future.successful(Some(mockCacheMap)))

      when(controller.dataCacheConnector.save[EstateAgentBusiness](any(), any(), any())
        (any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(Messages("error.expected.dateofchange.date.after.activitystartdate", "24-02-1990"))
    }

  }
}
