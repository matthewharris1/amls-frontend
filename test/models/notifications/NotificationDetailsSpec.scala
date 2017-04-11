package models.notifications

import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec


class NotificationDetailsSpec extends PlaySpec with MustMatchers {

  "the message text converter" must {
    "convert the input message text into the model" in {

      val inputString = "parameter1-1234|parameter2-ABC1234|Status-04-Approved"

      NotificationDetails.convertMessageText(inputString) mustBe Some(ReminderDetails(1234, "ABC1234"))

    }

    "return none when supplied with an invalid string" in {
      val inputString = "invalidtest"

      NotificationDetails.convertMessageText(inputString) must be(None)
    }
  }



}