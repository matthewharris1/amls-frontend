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

package views.renewal

import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import jto.validation.{Path, ValidationError}
import models.businessactivities.ExpectedBusinessTurnover
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.AmlsViewSpec
import views.Fixture


class business_turnoverSpec extends AmlsViewSpec with MustMatchers  {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addTokenForView()
  }

  "expected_business_turnover view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[ExpectedBusinessTurnover] = Form2(ExpectedBusinessTurnover.Third)

      def view = views.html.renewal.business_turnover(form2, true)

      doc.title must startWith(Messages("renewal.business-turnover.title") + " - " + Messages("summary.renewal"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[ExpectedBusinessTurnover] = Form2(ExpectedBusinessTurnover.Second)

      def view = views.html.renewal.business_turnover(form2, true)

      heading.html must be(Messages("renewal.business-turnover.title"))
      subHeading.html must include(Messages("summary.renewal"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "businessTurnover") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.renewal.business_turnover(form2, true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("businessTurnover")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")
    }

    "have a back link" in new ViewFixture {

      def view = views.html.renewal.business_turnover(EmptyForm, true)

      doc.getElementsByAttributeValue("class", "link-back") must not be empty
    }
  }
}
