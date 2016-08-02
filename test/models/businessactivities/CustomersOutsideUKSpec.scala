package models.businessactivities

import models.Country
import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.forms._
import play.api.data.mapping._
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsSuccess, Json}

class CustomersOutsideUKSpec extends PlaySpec {


  "CustomersOutsideUK" must {
    val rule = implicitly[Rule[UrlFormEncoded, CustomersOutsideUK]]
    val write = implicitly[Write[CustomersOutsideUK, UrlFormEncoded]]

    "round trip through Json correctly" in {

      val model: CustomersOutsideUK = CustomersOutsideUK(Some(Seq(Country("United Kingdom", "GB"))))

      Json.fromJson[CustomersOutsideUK](Json.toJson(model)) mustBe JsSuccess(model)
    }

    "round trip through forms correctly" in {

      val model: CustomersOutsideUK = CustomersOutsideUK(Some(Seq(Country("United Kingdom", "GB"))))

      rule.validate(write.writes(model)) mustBe Success(model)
    }

    "successfully validate when isOutside is false" in {

      val form: UrlFormEncoded = Map(
        "isOutside" -> Seq("false")
      )

      val model: CustomersOutsideUK = CustomersOutsideUK(None)

      rule.validate(form) mustBe Success(model)
    }

    "successfully validate when isOutside is true and there is at least 1 country selected" in {

      val form: UrlFormEncoded = Map(
        "isOutside" -> Seq("true"),
        "countries" -> Seq("GB")
      )

      val model: CustomersOutsideUK =
        CustomersOutsideUK(
          Some(Seq(Country("United Kingdom", "GB")))
        )

      rule.validate(form) mustBe Success(model)
    }

    "fail to validate when isOutside is true and there are no countries selected" in {

      val form: UrlFormEncoded = Map(
        "isOutside" -> Seq("true"),
        "countries" -> Seq.empty
      )

      rule.validate(form) mustBe Failure(
        Seq((Path \ "countries") -> Seq(ValidationError("error.invalid.ba.select.country")))
      )
    }

    "fail to validate when isOutside is true and there are more than 10 countries" in {

      val form: UrlFormEncoded = Map(
        "isOutside" -> Seq("true"),
        "countries[]" -> Seq.fill(11)("GB")
      )

      rule.validate(form) mustBe Failure(
        Seq((Path \ "countries") -> Seq(ValidationError("error.maxLength", 10)))
      )
    }

    "fail to validate when isOutside isn't selected" in {

      val form: UrlFormEncoded = Map.empty

      rule.validate(form) mustBe Failure(
        Seq((Path \ "isOutside") -> Seq(ValidationError("error.required.ba.select.country")))
      )
    }

    "successfully validate when there are empty values in the seq" in {

      val form: UrlFormEncoded = Map(
        "isOutside" -> Seq("true"),
        "countries[]" -> Seq("GB", "", "US", "")
      )

      rule.validate(form) mustBe Success(CustomersOutsideUK(Some(Seq(
        Country("United Kingdom", "GB"),
        Country("United States", "US")
      ))))
    }

    "test" in {

      val form: UrlFormEncoded = Map(
        "isOutside" -> Seq("true"),
        "countries[0]" -> Seq("GB"),
        "countries[1]" -> Seq("")
      )

      rule.validate(form) mustBe Success(CustomersOutsideUK(Some(Seq(
        Country("United Kingdom", "GB")
      ))))
    }
  }
}
