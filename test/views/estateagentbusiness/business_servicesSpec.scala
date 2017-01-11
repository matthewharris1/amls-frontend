package views.estateagentbusiness

import forms.{InvalidForm, ValidForm, Form2}
import models.estateagentbusiness._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class business_servicessSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "business_servicess view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[Services] = Form2(Services(Set()))

      def view = views.html.estateagentbusiness.business_servicess(form2, edit = true)

      doc.title must startWith(Messages("estateagentbusiness.services.title") + " - " + Messages("summary.estateagentbusiness"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[Services] = Form2(Services(Set()))

      def view = views.html.estateagentbusiness.business_servicess(form2, edit = true)

      heading.html must be(Messages("estateagentbusiness.services.title"))
      subHeading.html must include(Messages("summary.estateagentbusiness"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "services") -> Seq(ValidationError("not a message Key"))
        ))

      def view = views.html.estateagentbusiness.business_servicess(form2, edit = true)

      errorSummary.html() must include("not a message Key")

      doc.getElementById("services")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

    }
  }
}