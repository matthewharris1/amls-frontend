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

package views.businessmatching.updateservice

import forms.{EmptyForm, InvalidForm}
import jto.validation.{Path, ValidationError}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture

class select_activitiesSpec extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    override def view = views.html.businessmatching.updateservice.select_activities(EmptyForm,
        true,
        Set.empty[String],
        Set.empty[String]
      )

  }

  "The select_Activities view" must {

    "have the correct title" in new ViewFixture {
      doc.title must startWith(Messages("businessmatching.updateservice.selectactivities.title") + " - " + Messages("summary.updateservice"))
    }

    "have correct heading" in new ViewFixture {
      heading.html must be(Messages("businessmatching.updateservice.selectactivities.heading"))
    }

    "have correct subHeading" in new ViewFixture {
      subHeading.html must include(Messages("summary.updateservice"))
    }

    "show the correct content" in new ViewFixture {

    }

    "not show the return link" in new ViewFixture {
      override def view = views.html.businessmatching.updateservice.select_activities(EmptyForm,
          true,
          Set.empty[String],
          Set.empty[String]
        )
      doc.body().text() must not include Messages("link.return.registration.progress")
    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq((Path \ "businessmatching.updateservice.selectactivities") -> Seq(ValidationError("not a message Key"))))

      override def view = views.html.businessmatching.updateservice.select_activities(form2, true, Set.empty[String], Set.empty[String])

      errorSummary.html() must include("not a message Key")
    }
  }
}