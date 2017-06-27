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

package controllers.changeofficer

import connectors.DataCacheConnector
import models.responsiblepeople._
import play.api.inject.bind
import play.api.inject.guice.GuiceInjectorBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{AuthorisedFixture, GenericTestHelper}
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.Future

class StillEmployedControllerSpec extends GenericTestHelper {

  trait TestFixture extends AuthorisedFixture { self =>
    val request = addToken(self.authRequest)

    val cache = mock[DataCacheConnector]

    val injector = new GuiceInjectorBuilder()
      .overrides(bind[AuthConnector].to(self.authConnector))
      .overrides(bind[DataCacheConnector].to(self.cache))
      .build()

    lazy val controller = injector.instanceOf[StillEmployedController]

    val nominatedOfficer = ResponsiblePeople(
      personName = Some(PersonName("firstName", None, "lastName",None, None)),
      positions = Some(Positions(Set(NominatedOfficer),None))
    )

    val otherResponsiblePerson = ResponsiblePeople(
      personName = Some(PersonName("otherFirstName", None, "otherLastName",None, None)),
      positions = Some(Positions(Set(Director),None))
    )

    when(cache.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
      .thenReturn(Future.successful(Some(Seq(nominatedOfficer, otherResponsiblePerson))))
  }

  "The StillEmployedController" when {
    "get is called" must {

      "respond with OK and include the person name" in new TestFixture {

        val result = controller.get()(request)

        status(result) mustBe OK
        contentAsString(result) must include("firstName lastName")
      }

      "respond with an internal server error" when {
        "no nominated officer is found" in new TestFixture {

          when(cache.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(Seq(otherResponsiblePerson))))

          val result = controller.get()(request)

          status(result) mustBe INTERNAL_SERVER_ERROR

        }
      }
    }

    "post is called" must {
      "respond with SEE_OTHER" in new TestFixture {
        val result = controller.post()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RoleInBusinessController.get().url)
      }
    }
  }
}
