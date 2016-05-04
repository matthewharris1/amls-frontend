package models.asp

import models.registrationprogress.{Started, Completed, NotStarted, Section}
import typeclasses.MongoKey
import uk.gov.hmrc.http.cache.client.CacheMap

case class Asp(
              otherBusinessTaxMatters: Option[OtherBusinessTaxMatters] = None
              ) {

  def otherBusinessTaxMatters(p: OtherBusinessTaxMatters): Asp =
    this.copy(otherBusinessTaxMatters = Some(p))

  def isComplete: Boolean = this match {
      case Asp(Some(_)) => true
      case _ => false
  }

}

object Asp {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  def section(implicit cache: CacheMap): Section = {
    val messageKey = "asp"
    val notStarted = Section(messageKey, NotStarted, controllers.asp.routes.WhatYouNeedController.get())
    cache.getEntry[Asp](key).fold(notStarted) {
      model =>
        if (model.isComplete) {
          //TODO: Update this route to correct page.
          Section(messageKey, Completed, controllers.routes.RegistrationProgressController.get())
        } else {
          //TODO: Update this route to correct page.
          Section(messageKey, Started, controllers.asp.routes.WhatYouNeedController.get())
        }
    }
  }

  val key = "asp"

  implicit val mongoKey = new MongoKey[Asp] {
    override def apply(): String = "asp"
  }

  implicit val format = Json.format[Asp]

  implicit def default(details: Option[Asp]): Asp =
    details.getOrElse(Asp())
}
