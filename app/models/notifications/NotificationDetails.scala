package models.notifications

import models.confirmation.Currency
import models.notifications.ContactType.{RegistrationVariationApproval, ApplicationAutorejectionForFailureToPay, DeRegistrationEffectiveDateChange}
import models.notifications.StatusType.DeRegistered
import org.joda.time.LocalDate
import play.api.libs.json.Json

case class NotificationDetails(contactType : Option[ContactType],
                               status : Option[Status],
                               messageText : Option[String],
                               variation : Boolean) {

  def getContactType: ContactType = {

    val statusReason = for {
      st <- status
      reason <- st.statusReason
    } yield reason

    contactType.getOrElse(
      (status, statusReason, variation) match {
        case (Some(Status(Some(DeRegistered),_)),_,_) => DeRegistrationEffectiveDateChange
        case (_, Some(r),_) => ApplicationAutorejectionForFailureToPay
        case (_,_, true) => RegistrationVariationApproval
        case _ => throw new RuntimeException("No matching ContactType found")
      }
    )
  }

  def subject = {
    s"notifications.subject.$getContactType"
  }
}

object NotificationDetails {

  def convertEndDateMessageText(inputString: String): Option[EndDateDetails] = {
    Some(EndDateDetails(new LocalDate(2018,7,31), Some("testref")))
  }

  def convertMessageText(inputString: String): Option[ReminderDetails] = {
    inputString.split("\\|").toList match {
      case amount::ref::tail =>
        Some(ReminderDetails(Currency(splitByDash(amount).toDouble),splitByDash(ref)))
      case _ => None
    }
  }

  private def splitByDash(s: String): String = s.split("-")(1)

  implicit val reads = Json.reads[NotificationDetails]
}
