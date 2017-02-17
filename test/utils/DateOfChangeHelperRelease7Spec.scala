package utils

import models.aboutthebusiness.{RegisteredOffice, RegisteredOfficeUK}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication
import uk.gov.hmrc.play.test.UnitSpec

class DateOfChangeHelperRelease7Spec extends UnitSpec with OneAppPerSuite with MockitoSugar {

  override lazy val app = FakeApplication(additionalConfiguration = Map("Test.microservice.services.feature-toggle.release7" -> true) )

  "DateOfChangeHelper" should {

    object DateOfChangeHelperTest extends DateOfChangeHelper{
    }

    val originalModel =RegisteredOfficeUK(
      "addressLine1",
      "addressLine2",
      None,
      None,
      "postCode",
      None
    )


    val changeModel = RegisteredOfficeUK("","",None, None, "", None)

    "return true" when {
      "a change has been made to a model" in {
        DateOfChangeHelperTest.redirectToDateOfChange[RegisteredOffice](Some(originalModel), changeModel) should be(true)
      }
    }

    "return false" when {
      "no change has been made to a model" in {
        DateOfChangeHelperTest.redirectToDateOfChange[RegisteredOffice](Some(originalModel), originalModel) should be(false)
      }
    }

  }

}