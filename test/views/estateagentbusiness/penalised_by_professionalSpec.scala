package views.estateagentbusiness

import forms.{InvalidForm, ValidForm, Form2}
import models.estateagentbusiness._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class penalised_by_professionalSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "penalised_by_professional view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[ProfessionalBody] = Form2(ProfessionalBodyNo)

      def view = views.html.estateagentbusiness.penalised_by_professional(form2, edit = true)

      doc.title must startWith(Messages("estateagentbusiness.penalisedbyprofessional.title") + " - " + Messages("summary.estateagentbusiness"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[ProfessionalBody] = Form2(ProfessionalBodyNo)

      def view = views.html.estateagentbusiness.penalised_by_professional(form2, edit = true)

      heading.html must be(Messages("estateagentbusiness.penalisedbyprofessional.title"))
      subHeading.html must include(Messages("summary.estateagentbusiness"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "penalised") -> Seq(ValidationError("not a message Key")),
          (Path \ "professionalBody") -> Seq(ValidationError("not another message key"))
        ))

      def view = views.html.estateagentbusiness.penalised_by_professional(form2, edit = true)

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("not another message key")

      doc.getElementById("penalised")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

      doc.getElementById("professionalBody")
        .parent()
        .getElementsByClass("error-notification").first().html() must include("not another message key")
    }
  }
}