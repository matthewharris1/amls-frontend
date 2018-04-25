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

package views.businessmatching

import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import jto.validation.{Path, ValidationError}
import models.businessmatching.{MsbServices, TransmittingMoney}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture
import views.html.businessmatching.updateservice.add.msb_subservices


class msb_subservicesSpec extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    def view = msb_subservices(EmptyForm, edit = false)
  }

  "The msb_subservices view" must {

    "have correct title" in new ViewFixture {

      val form2: ValidForm[MsbServices] = Form2(MsbServices(Set(TransmittingMoney)))

      override def view = views.html.businessmatching.updateservice.add.msb_subservices(form2, edit = false)

      doc.title must startWith(Messages("businessmatching.updateservice.msb.services.title") + " - " + Messages("summary.businessmatching"))
      heading.html must be(Messages("businessmatching.updateservice.msb.services.title"))
      subHeading.html must include(Messages("summary.businessmatching"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "msbServices") -> Seq(ValidationError("not a message Key"))
        ))

      override def view = views.html.businessmatching.updateservice.add.msb_subservices(form2, edit = false)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("msbServices")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

    }
    "hide the return to progress link" in new ViewFixture {
      val form2: ValidForm[MsbServices] = Form2(MsbServices(Set(TransmittingMoney)))

      override def view = views.html.businessmatching.updateservice.add.msb_subservices(form2, edit = false, showReturnLink = false)

      doc.body().text() must not include Messages("link.return.registration.progress")
    }
  }
}