package models.aboutthebusiness

import org.joda.time.LocalDate
import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.{Failure, Path, Success}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess, Json}


class ActivityStartDateSpec extends PlaySpec {
  // scalastyle:off

  "Form validation" must {
    "pass validation" when {
      "given a valid date" in {

        val model = Map(
          "startDate.day" -> Seq("24"),
          "startDate.month" -> Seq("2"),
          "startDate.year" -> Seq("1990")
        )

        ActivityStartDate.formRule.validate(model) must be(Success(ActivityStartDate(new LocalDate(1990, 2, 24))))
      }
    }

    "fail validation" when {
      "given a day value with too many numerical characters" in {

        val model = Map(
          "startDate.day" -> Seq("2466"),
          "startDate.month" -> Seq("2"),
          "startDate.year" -> Seq("1990")
        )

        ActivityStartDate.formRule.validate(model) must be(Failure(Seq(Path \ "startDate" -> Seq(
          ValidationError("error.expected.jodadate.format", "yyyy-MM-dd")))))
      }

      "given missing data represented by an empty string" in {

        val model = Map(
          "startDate.day" -> Seq(""),
          "startDate.month" -> Seq(""),
          "startDate.year" -> Seq("")
        )
        ActivityStartDate.formRule.validate(model) must be(Failure(Seq(
          Path \ "startDate" -> Seq(ValidationError("error.expected.jodadate.format", "yyyy-MM-dd"))
        )))
      }
    }

    "successfully write the model" in {

      ActivityStartDate.formWrites.writes(ActivityStartDate(new LocalDate(1990, 2, 24))) mustBe Map(
        "startDate.day" -> Seq("24"),
        "startDate.month" -> Seq("2"),
        "startDate.year" -> Seq("1990")
      )
    }
  }

  "Json validation" must {

    "Read and write successfully" in {

      ActivityStartDate.format.reads(ActivityStartDate.format.writes(ActivityStartDate(new LocalDate(1990, 2, 24)))) must be(
        JsSuccess(ActivityStartDate(new LocalDate(1990, 2, 24)), JsPath \ "startDate"))
    }

    "write successfully" in {
      ActivityStartDate.format.writes(ActivityStartDate(new LocalDate(1990, 2, 24))) must be(Json.obj("startDate" -> "1990-02-24"))
    }
  }
}
