/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.responsiblepeople.address

import connectors.DataCacheConnector
import controllers.actions.SuccessfulAuthAction
import models.responsiblepeople._
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AmlsSpec, AuthAction, AuthorisedFixture}

import scala.concurrent.Future

class NewHomeAddressDateOfChangeControllerSpec extends AmlsSpec {

  trait Fixture {
    self =>
    val request = addToken(authRequest)
    val dataCacheConnector = mock[DataCacheConnector]

    lazy val app = new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .overrides(bind[DataCacheConnector].to(dataCacheConnector))
      .overrides(bind[AuthAction].to(SuccessfulAuthAction))
      .build()

    val controller = app.injector.instanceOf[NewHomeAddressDateOfChangeController]

    val cacheMap = mock[CacheMap]
  }

  "NewHomeAddressDateOfChangeController" must {

    val responsiblePeople = ResponsiblePerson(personName = Some(PersonName("FirstName",
      None, "lastName")),
      positions = Some(Positions(Set(BeneficialOwner),Some(PositionStartDate(new LocalDate(2009,1,1))))))

    "Get:" must {
      "successfully load when the person moved to new address page" when {
        "date of change model exists in mongoCache" in new Fixture {

          when(cacheMap.getEntry[Seq[ResponsiblePerson]](any())(any()))
            .thenReturn(Some(Seq(responsiblePeople)))
          when(cacheMap.getEntry[NewHomeDateOfChange](NewHomeDateOfChange.key)).thenReturn(Some(NewHomeDateOfChange(Some(LocalDate.now()))))
          when(controller.dataCacheConnector.fetchAll(any())(any())).thenReturn(Future.successful(Some(cacheMap)))

          val result = controller.get(1)(request)
          status(result) must be(OK)

        }

        "date of change model in not persisted in mongoCache" in new Fixture {

          when(cacheMap.getEntry[Seq[ResponsiblePerson]](any())(any()))
            .thenReturn(Some(Seq(responsiblePeople)))
          when(cacheMap.getEntry[NewHomeDateOfChange](NewHomeDateOfChange.key)).thenReturn(None)
          when(controller.dataCacheConnector.fetchAll(any())(any())).thenReturn(Future.successful(Some(cacheMap)))

          val result = controller.get(1)(request)
          status(result) must be(OK)
        }

        "load NotFound view when index is not in range" in new Fixture {
          when(cacheMap.getEntry[Seq[ResponsiblePerson]](any())(any()))
            .thenReturn(None)
          when(cacheMap.getEntry[NewHomeDateOfChange](NewHomeDateOfChange.key)).thenReturn(None)
          when(controller.dataCacheConnector.fetchAll(any())(any())).thenReturn(Future.successful(Some(cacheMap)))

          val result = controller.get(1)(request)
          status(result) must be(NOT_FOUND)
        }
      }
    }

    "Post:" must {
      "redirect to next page successfully for valid input" in new Fixture {
        val postRequest = requestWithUrlEncodedBody(
          "dateOfChange.day" -> "20",
          "dateOfChange.month" -> "5",
          "dateOfChange.year" -> "2014"
        )
        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())
          (any(), any())).thenReturn(Future.successful(Some(Seq(responsiblePeople))))

        when(controller.dataCacheConnector.save[NewHomeDateOfChange](any(), any(), any())
          (any(), any())).thenReturn(Future.successful(cacheMap))
        val result = controller.post(1)(postRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.responsiblepeople.address.routes.NewHomeAddressController.get(1).url))
      }

      "fail validation on invalid input" in new Fixture {
        val postRequest = requestWithUrlEncodedBody(
          "dateOfChange.month" -> "10",
          "dateOfChange.day" -> "01"
        )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(Seq(responsiblePeople))))
          when(controller.dataCacheConnector.save[NewHomeDateOfChange](any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(cacheMap))

        val result = controller.post(1)(postRequest)

        status(result) must be(BAD_REQUEST)

      }
    }
  }
}
