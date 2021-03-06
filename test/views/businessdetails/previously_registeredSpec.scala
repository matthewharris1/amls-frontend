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

package views.businessdetails

import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import jto.validation.{Path, ValidationError}
import models.businessdetails.{PreviouslyRegistered, PreviouslyRegisteredYes}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.AmlsViewSpec
import views.Fixture


class previously_registeredSpec extends AmlsViewSpec with MustMatchers  {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addTokenForView()
  }

  "previously_registered view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[PreviouslyRegistered] = Form2(PreviouslyRegisteredYes(Some("prevMLRRegNo")))

      def view = views.html.businessdetails.previously_registered(form2, true)

      doc.title must startWith(Messages("businessdetails.registeredformlr.title") + " - " + Messages("summary.businessdetails"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[PreviouslyRegistered] = Form2(PreviouslyRegisteredYes(Some("prevMLRRegNo")))

      def view = views.html.businessdetails.previously_registered(form2, true)

      heading.html must be(Messages("businessdetails.registeredformlr.title"))
      subHeading.html must include(Messages("summary.businessdetails"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "previouslyRegistered") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.businessdetails.previously_registered(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("previouslyRegistered")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

    }

    "have a back link" in new ViewFixture {
      val form2: Form2[_] = EmptyForm

      def view = views.html.businessdetails.previously_registered(form2, true)

      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }
  }
}
