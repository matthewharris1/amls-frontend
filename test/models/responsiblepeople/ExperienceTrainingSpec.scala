package models.responsiblepeople

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.{Failure, Path, Success}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class ExperienceTrainingSpec extends PlaySpec with MockitoSugar {

  "Form Validation" must {

    "experienceInformation" must {

      "successfully validate" in {

        ExperienceTraining.experienceInformationType.validate("did the training for the business activities. Dont know when") must
          be(Success("did the training for the business activities. Dont know when"))
      }

      "fail to validate an empty string" in {

        ExperienceTraining.experienceInformationType.validate("") must
          be(Failure(Seq(
            Path -> Seq(ValidationError("error.required.rp.experiencetraining.information"))
          )))
      }

      "fail to validate a string longer than 255 characters" in {

        ExperienceTraining.experienceInformationType.validate("A" * 256) must
          be(Failure(Seq(
            Path -> Seq(ValidationError("error.invalid.length.rp.experiencetraining.information"))
          )))
      }
    }

    "successfully validate given an enum value" in {
      ExperienceTraining.formRule.validate(Map("experienceTraining" -> Seq("false"))) must
        be(Success(ExperienceTrainingNo))
    }

    "successfully validate given a `Yes` value" in {
      val data = Map(
        "experienceTraining" -> Seq("true"),
        "experienceInformation" -> Seq("0123456789")
      )

      ExperienceTraining.formRule.validate(data) must be(Success(ExperienceTrainingYes("0123456789")))
    }

    "successfully validate given a `No` value" in {
      val data = Map(
        "experienceTraining" -> Seq("false")
      )

      ExperienceTraining.formRule.validate(data) must be(Success(ExperienceTrainingNo))
    }

    "fail to validate given a `Yes` with no value" in {

      val data = Map(
        "experienceTraining" -> Seq("true")
      )

      ExperienceTraining.formRule.validate(data) must
        be(Failure(Seq(
          (Path \ "experienceInformation") -> Seq(ValidationError("error.required"))
        )))
    }

    "fail to validate given neither 'Yes' nor 'No' value" in {

      val data: Map[String, Seq[String]] = Map.empty[String, Seq[String]]

      ExperienceTraining.formRule.validate(data) must
        be(Failure(Seq(
          (Path \ "experienceTraining") -> Seq(ValidationError("error.required.rp.experiencetraining"))
        )))
    }

    "write correct data from enum value" in {

      ExperienceTraining.formWrites.writes(ExperienceTrainingNo) must be(Map("experienceTraining" -> Seq("false")))

    }

    "write correct data from `Yes` value" in {

      ExperienceTraining.formWrites.writes(ExperienceTrainingYes("0123456789")) must
        be(Map("experienceTraining" -> Seq("true"), "experienceInformation" -> Seq("0123456789")))
    }

  }

  "JSON validation" must {
    "successfully validate given an enum value" in {

      Json.fromJson[ExperienceTraining](Json.obj("experienceTraining" -> false)) must
        be(JsSuccess(ExperienceTrainingNo, JsPath \ "experienceTraining"))
    }

    "successfully validate given an `Yes` value" in {

      val json = Json.obj("experienceTraining" -> true, "experienceInformation" -> "0123456789")

      Json.fromJson[ExperienceTraining](json) must
        be(JsSuccess(ExperienceTrainingYes("0123456789"), JsPath \ "experienceTraining" \ "experienceInformation"))
    }

    "fail to validate when given an empty `Yes` value" in {

      val json = Json.obj("experienceTraining" -> true)

      Json.fromJson[ExperienceTraining](json) must
        be(JsError((JsPath \ "experienceTraining" \ "experienceInformation") -> ValidationError("error.path.missing")))
    }

    "write the correct value for Yes" in {

      Json.toJson(ExperienceTrainingYes("0123456789")) must
        be(Json.obj(
          "experienceTraining" -> true,
          "experienceInformation" -> "0123456789"
        ))
    }

    "write the correct value for No" in {
      Json.toJson(ExperienceTrainingNo) must be(Json.obj("experienceTraining" -> false))

      val json = Json.obj("experienceTraining" -> false)

      Json.fromJson[ExperienceTraining](json) must
        be(JsSuccess(ExperienceTrainingNo, JsPath \ "experienceTraining"))

    }

  }

}
