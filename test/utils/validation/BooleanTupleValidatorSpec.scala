package utils.validation

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.data.FormError
import utils.validation.BooleanTupleValidator._

class BooleanTupleValidatorSpec extends PlaySpec with MockitoSugar with OneServerPerSuite {

  private val ValidValuesForRadioButton = Seq("First", "Second", "Third")
  private val TupleMapping = Seq((true, true), (true, false), (false, true))
  private val InvalidValuesForRadioButton = Seq("Invalid", "****")
  private val mappedValuesForBinding: Seq[(String, (Boolean, Boolean))] = ValidValuesForRadioButton.zip(TupleMapping)
  
  "BooleanTupleValidator bind" should {

    "accept valid selections" in {
      val mapping = mandatoryBooleanTuple(mappedValuesForBinding)
      mappedValuesForBinding.foreach { x =>
        mapping.bind(Map("" -> x._1)) mustBe Right(x._2)
      }
    }

    "reject invalid selection" in {
      val mapping = mandatoryBooleanTuple(mappedValuesForBinding)
      InvalidValuesForRadioButton.foreach { x =>
        mapping.bind(Map("" -> x)).left.getOrElse(Nil) must contain(FormError("", "error.something"))
      }
    }
  }

  "BooleanTupleValidator unbind" should {
    "accept valid values" in {
      val mapping = mandatoryBooleanTuple(mappedValuesForBinding)
      InvalidValuesForRadioButton.foreach { x =>
        mapping.binder.unbind("", (true, true)) mustBe Map("" -> "First")
      }
    }
  }

}
