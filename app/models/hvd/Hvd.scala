package models.hvd

import models.registrationprogress.{Started, Completed, NotStarted, Section}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap


case class Hvd (cashPayment: Option[CashPayment] = None,) {

  def isComplete: Boolean =
    this match {
      case Hvd(_) => true
      case _ => false
    }
}

object Hvd {

  val key = "hvd"

  def section(implicit cache: CacheMap): Section = {
    val notStarted = Section(key, NotStarted, controllers.hvd.routes.WhatYouNeedController.get())
    cache.getEntry[Hvd](key).fold(notStarted) {
      case model if model.isComplete =>
        Section(key, Completed, controllers.hvd.routes.SummaryController.get())
      case _ =>
        Section(key, Started, controllers.hvd.routes.WhatYouNeedController.get())
    }
  }

  implicit val format = Json.format[Hvd]

  implicit def default(hvd: Option[Hvd]): Hvd =
    hvd.getOrElse(Hvd())
}


