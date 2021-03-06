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

package views.responsiblepeople

import forms.{EmptyForm, InvalidForm, Form2}
import org.scalatest.{MustMatchers}
import utils.AmlsViewSpec
import jto.validation.Path
import jto.validation.ValidationError
import play.api.i18n.Messages
import views.Fixture

class fit_and_properSpec extends AmlsViewSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addTokenForView()
  }

  "fit_and_proper view" must {
    "have a back link" in new ViewFixture {
      val form2: Form2[_] = EmptyForm
      def view = views.html.responsiblepeople.fit_and_proper(form2, true, 0, None, "PersonName")
      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }

    "have correct title" in new ViewFixture {
      val form2: Form2[_] = EmptyForm
      def view = views.html.responsiblepeople.fit_and_proper(form2, true, 0, None, "PersonName")
      doc.title must be(
        Messages("responsiblepeople.fit_and_proper.title", "PersonName")
        + " - " + Messages("summary.responsiblepeople")+
        " - " + Messages("title.amls") +
        " - " + Messages("title.gov")
      )
    }

    "have correct headings" in new ViewFixture {
      val form2: Form2[_] = EmptyForm
      def view = views.html.responsiblepeople.fit_and_proper(form2, true, 0, None, "PersonName")
      heading.html must be(Messages("responsiblepeople.fit_and_proper.heading", "PersonName"))
      subHeading.html must include(Messages("summary.responsiblepeople"))
      doc.title must include(Messages("responsiblepeople.fit_and_proper.title"))
    }

    "have the correct content" in new ViewFixture {
        val form2: Form2[_] = EmptyForm
        def view = views.html.responsiblepeople.fit_and_proper(form2, true, 0, None, "PersonName")
        doc.body().html() must include(Messages("responsiblepeople.fit_and_proper.details"))
        doc.body().html() must include(Messages("responsiblepeople.fit_and_proper.details2", "PersonName"))
    }

    "show errors in the correct locations" in new ViewFixture {
      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "hasAlreadyPassedFitAndProper") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.responsiblepeople.fit_and_proper(form2, true, 0, None, "PersonName")
      errorSummary.html() must include("not a message Key")
      doc.getElementById("hasAlreadyPassedFitAndProper")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")
    }
  }
}