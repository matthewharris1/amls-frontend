package models.moneyservicebusiness

import org.scalatestplus.play.PlaySpec
import play.api.data.mapping.{Failure, Path, Success}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess}

class CETransactionInNext12MonthsSpec extends PlaySpec {

  "CETransactionInNext12Months" should {

    "Form Validation" must {

      "Successfully read form data for option yes" in {

        val map = Map("ceTransaction" -> Seq("12345678963"))

        CETransactionsInNext12Months.formRule.validate(map) must be(Success(CETransactionsInNext12Months("12345678963")))
      }

      "fail validation on missing field" in {

        val map = Map("ceTransaction" -> Seq(""))

        CETransactionsInNext12Months.formRule.validate(map) must be(Failure(
          Seq( Path \ "ceTransaction" -> Seq(ValidationError("error.required.msb.transactions.in.12months")))))
      }

      "fail validation on invalid field" in {

        val map = Map("ceTransaction" -> Seq("asas"))
        CETransactionsInNext12Months.formRule.validate(map) must be(Failure(
          Seq( Path \ "ceTransaction" -> Seq(ValidationError("error.invalid.msb.transactions.in.12months")))))
      }

      "fail validation on invalid field when it exceeds the max length" in {

        val map = Map("ceTransaction" -> Seq("123"*10))
        CETransactionsInNext12Months.formRule.validate(map) must be(Failure(
          Seq( Path \ "ceTransaction" -> Seq(ValidationError("error.invalid.msb.transactions.in.12months")))))
      }

      "fail validation on invalid field1" in {

        val map = Map("ceTransaction" -> Seq("123456"))
        CETransactionsInNext12Months.formRule.validate(map) must be(Success(CETransactionsInNext12Months("123456")))
      }


      "successfully write form data" in {

        CETransactionsInNext12Months.formWrites.writes(CETransactionsInNext12Months("12345678963")) must be(Map("ceTransaction" -> Seq("12345678963")))
      }
    }

    "Json Validation" must {

      "Successfully read/write Json data" in {

        CETransactionsInNext12Months.format.reads(CETransactionsInNext12Months.format.writes(
          CETransactionsInNext12Months("12345678963"))) must be(JsSuccess(CETransactionsInNext12Months("12345678963"), JsPath \ "ceTransaction"))

      }
    }
  }
}