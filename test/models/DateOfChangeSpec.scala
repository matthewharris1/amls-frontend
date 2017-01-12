package models

import org.joda.time.LocalDate
import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.{Failure, Path, Success}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsString, _}


class DateOfChangeSpec extends PlaySpec {
  "DateOfChange" must {

    "read the form correctly when given a valid date" in {
      val model =    Map (
        "dateOfChange.day" -> Seq("24"),
        "dateOfChange.month" -> Seq("2"),
        "dateOfChange.year" -> Seq("1990")
      )

      DateOfChange.formRule.validate(model) must be (Success(DateOfChange(new LocalDate(1990, 2, 24))))

    }

    "fail form validation when given a future date" in {
      val model =    Map (
        "dateOfChange.day" -> Seq("24"),
        "dateOfChange.month" -> Seq("2"),
        "dateOfChange.year" -> Seq(LocalDate.now().plusYears(1).getYear.toString)
      )

      DateOfChange.formRule.validate(model) must be(
        Failure(
          Seq(
            Path \ "dateOfChange" -> Seq(ValidationError("error.future.date")))
        ))

    }

    "fail form validation when given a date before a business activities start date" in {
      val model = Map (
        "dateOfChange.day" -> Seq("24"),
        "dateOfChange.month" -> Seq("2"),
        "dateOfChange.year" -> Seq("2012"),
        "activityStartDate" -> Seq("2016-05-25")
      )

      DateOfChange.formRule.validate(model) must be(
        Failure(
          Seq(
            Path \ "dateOfChange" -> Seq(ValidationError("error.expected.regofficedateofchange.date.after.activitystartdate", "25-05-2016")))
        ))
    }

    "read from JSON correctly" in {

      val json = JsString("2016-02-24")

      val result = Json.fromJson[DateOfChange](json)
      result.get.dateOfChange must be(new LocalDate(2016,2,24))
    }

    "write to JSON correctly" in {

      val date = DateOfChange(new LocalDate(2016,2,24))
      val json = JsString("2016-02-24")

      val result = Json.toJson(date)
      result must be(json)
    }
  }
}
