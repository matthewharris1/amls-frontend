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

package views.tcsp

import forms.{EmptyForm, InvalidForm}
import jto.validation.{Path, ValidationError}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.AmlsSpec
import views.Fixture


class complex_corp_structure_creationSpec extends AmlsSpec with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
  }

  "complex_corp_structure_creation view" must {
    "have correct title, heading amd subheading" in new ViewFixture {

      val form2 = EmptyForm

      def view = views.html.tcsp.complex_corp_structure_creation(form2, true)

      doc.getElementsByAttributeValue("class", "link-back") must not be empty

      val title = Messages("tcsp.create.complex.corporate.structures.lbl") + " - " + Messages("summary.tcsp") + " - " +
        Messages("title.amls") + " - " + Messages("title.gov")

      doc.title must be(title)
      heading.html must be(Messages("tcsp.create.complex.corporate.structures.lbl"))
      subHeading.html must include(Messages("summary.tcsp"))
    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "complexCorpStructureCreation") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.tcsp.complex_corp_structure_creation(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("complexCorpStructureCreation")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")
    }
  }
}