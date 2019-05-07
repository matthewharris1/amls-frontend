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

package views.status

import forms.EmptyForm
import generators.AmlsReferenceNumberGenerator
import models.FeeResponse
import models.ResponseType.SubscriptionResponseType
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.{DateHelper, AmlsSpec}
import views.Fixture

class status_submittedSpec extends AmlsSpec with MustMatchers with AmlsReferenceNumberGenerator{

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val feeResponse = FeeResponse(
      SubscriptionResponseType,
      amlsRegistrationNumber,
      150.00,
      Some(100.0),
      None,
      300.0,
      550.0,
      Some("XA000000000000"),
      None,
      new DateTime(2017, 12, 1, 1, 3, DateTimeZone.UTC)
    )

  }

  "status_submitted view" must {

    val pageTitleSuffix = " - Your registration - " + Messages("title.amls") + " - " + Messages("title.gov")

    "have correct title, heading and sub heading" in new ViewFixture {

      val form2 = EmptyForm

      def view = views.html.status.status_submitted(amlsRegistrationNumber, Some("business Name"), Some(feeResponse))

      doc.title must be(Messages("status.submissionreadyforreview.heading") + pageTitleSuffix)
      heading.html must be(Messages("status.submissionreadyforreview.heading"))
      subHeading.html must include(Messages("summary.status"))
    }

    "contain content elements" in new ViewFixture {

      def view = views.html.status.status_submitted(amlsRegistrationNumber, Some("business Name"), Some(feeResponse))

      doc.getElementsContainingOwnText("business Name").hasText must be(true)
      doc.getElementsContainingOwnText(Messages("status.business")).hasText must be(true)

      doc.getElementsByClass("heading-secondary").first().html() must include(Messages("summary.status"))
      doc.getElementById("status-submitted-business").html() must be(Messages("status.business"))
      doc.getElementById("status-submitted-business-name").html() must be("business Name")

      doc.getElementsByClass("list").first().child(0).html() must include(Messages("status.complete"))
      doc.getElementsByClass("list").first().child(1).html() must include(Messages("status.submitted"))
      doc.getElementsByClass("list").first().child(2).html() must include(Messages("status.underreview"))

      for (index <- 0 to 1) {
        doc.getElementsByClass("status-list").first().child(index).hasClass("status-list--complete") must be(true)
      }

      doc.getElementsByClass("status-list").first().child(2).hasClass("status-list--end") must be(true)

      doc.getAllElements().html() must include(Messages("status.submissionreadyforreview.description"))
      doc.getAllElements().html() must include(Messages("status.submissionreadyforreview.description.3"))
      Option(doc.getElementById("submission-ready-pay-the-fee")) mustBe defined
      doc.getElementsMatchingOwnText(Messages("notifications.youHaveMessages")).hasAttr("href") must be(true)
      doc.getElementsMatchingOwnText(Messages("notifications.youHaveMessages")).attr("href") mustBe controllers.routes.NotificationController.getMessages().url
    }


    "contain the no fee response content elements" in new ViewFixture {

      def view = views.html.status.status_submitted(amlsRegistrationNumber, Some("business Name"), None)

      doc.getElementsContainingOwnText("business Name").hasText must be(true)
      doc.getElementsContainingOwnText(Messages("status.business")).hasText must be(true)

      doc.getElementsByClass("heading-secondary").first().html() must include(Messages("summary.status"))
      doc.getElementById("status-submitted-business").html() must be(Messages("status.business"))
      doc.getElementById("status-submitted-business-name").html() must be("business Name")

      doc.getElementsByClass("list").first().child(0).html() must include(Messages("status.complete"))
      doc.getElementsByClass("list").first().child(1).html() must include(Messages("status.submitted"))
      doc.getElementsByClass("list").first().child(2).html() must include(Messages("status.underreview"))

      for (index <- 0 to 1) {
        doc.getElementsByClass("status-list").first().child(index).hasClass("status-list--complete") must be(true)
      }

      doc.getElementsByClass("status-list").first().child(2).hasClass("status-list--end") must be(true)

      doc.getAllElements().html() must include(Messages("status.submissionreadyforreview.nofee.description"))
      doc.getAllElements().html() must include(Messages("status.submissionreadyforreview.description.3"))
      doc.getAllElements().html() must include(Messages("status.submissionreadyforreview.description.4"))
      Option(doc.getElementsByClass("partial-deskpro-form").first()) mustBe defined

      doc.getElementsMatchingOwnText(Messages("notifications.youHaveMessages")).hasAttr("href") must be(true)
      doc.getElementsMatchingOwnText(Messages("notifications.youHaveMessages")).attr("href") mustBe controllers.routes.NotificationController.getMessages().url
    }



    "contain 'update/amend information' content and link" in new ViewFixture {

      def view = views.html.status.status_submitted(amlsRegistrationNumber, Some("business Name"), None)

      doc.getElementsByClass("statusblock").first().html() must include(Messages("status.hassomethingchanged"))
      doc.getElementsByClass("statusblock").first().html() must include(Messages("status.amendment.edit"))
    }

    "contain survey link for supervised status" in new ViewFixture {
      def view =  views.html.status.status_submitted(amlsRegistrationNumber, Some("business Name"), Some(feeResponse))

      doc.getElementsMatchingOwnText(Messages("survey.satisfaction.beforeyougo")).text() must
        be(Messages("survey.satisfaction.beforeyougo"))

      doc.getElementsMatchingOwnText(Messages("survey.satisfaction.beforeyougo")).hasAttr("href") must be(true)
      doc.getElementsMatchingOwnText(Messages("survey.satisfaction.beforeyougo")).attr("href") must be("/anti-money-laundering/satisfaction-survey")
    }

    "show specific content" when {
      "view input has feeData and submitted date" in new ViewFixture {

        def view = views.html.status.status_submitted(amlsRegistrationNumber, Some("business name"), Some(feeResponse))
        val date = DateHelper.formatDate(feeResponse.createdAt.toLocalDate)
        doc.getElementsMatchingOwnText(Messages("status.submittedForReview.submitteddate.text")).text must
          be(Messages("status.submittedForReview.submitteddate.text", date))
      }

      "view input is none" in new ViewFixture {

        def view = views.html.status.status_submitted(amlsRegistrationNumber, None, None)

        doc.getElementById("status-submitted-business") must be(null)
        doc.getElementById("status-submitted-business-name") must be(null)

        doc.getElementsContainingOwnText(Messages("status.submittedForReview.submitteddate.text")).isEmpty must be(true)

        doc.getElementsByTag("details").text() must include(Messages("fee.details.dup_nofees.heading"))
        doc.html must include(Messages("status.submissionreadyforreview.nofee.description.link"))
        doc.html must include(Messages("fee.details.dup_nofees.heading"))
      }

    }

  }

}
