package views.businessactivities

import forms.{InvalidForm, ValidForm, Form2}
import models.businessactivities.HowManyEmployees
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.mapping.Path
import play.api.data.validation.ValidationError
import play.api.i18n.Messages
import views.ViewFixture


class business_employeesSpec extends WordSpec with MustMatchers with OneAppPerSuite {

  "business_employees view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[HowManyEmployees] = Form2(HowManyEmployees("ECount", "SCount"))

      def view = views.html.businessactivities.business_employees(form2, true)

      doc.title must startWith(Messages("businessactivities.employees.title") + " - " + Messages("summary.businessactivities"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[HowManyEmployees] = Form2(HowManyEmployees("ECount", "SCount"))

      def view = views.html.businessactivities.business_employees(form2, true)

      heading.html must be(Messages("businessactivities.employees.title"))
      subHeading.html must include(Messages("summary.businessactivities"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "employeeCount") -> Seq(ValidationError("not a message Key")),
          (Path \ "employeeCountAMLSSupervision") -> Seq(ValidationError("second not a message Key"))
        ))

      def view = views.html.businessactivities.business_employees(form2, true)

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")

      doc.getElementById("employeeCount")
        .parent()
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

      doc.getElementById("employeeCountAMLSSupervision")
        .parent()
        .getElementsByClass("error-notification").first().html() must include("second not a message Key")
    }
  }
}