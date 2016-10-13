package models.tradingpremises

import models.registrationprogress.{Completed, NotStarted, Section, Started}
import typeclasses.MongoKey
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.StatusConstants

import scala.collection.Seq

case class TradingPremises(
                            registeringAgentPremises: Option[RegisteringAgentPremises] = None,
                            yourTradingPremises: Option[YourTradingPremises] = None,
                            businessStructure: Option[BusinessStructure] = None,
                            agentName: Option[AgentName] = None,
                            agentCompanyName: Option[AgentCompanyName] = None,
                            agentPartnership: Option[AgentPartnership] = None,
                            whatDoesYourBusinessDoAtThisAddress : Option[WhatDoesYourBusinessDo] = None,
                            msbServices: Option[MsbServices] = None,
                            hasChanged: Boolean = false,
                            lineId: Option[Int] = None,
                            status: Option[String] = None,
                            endDate:Option[ActivityEndDate] = None
                          ) {

  def businessStructure(p: BusinessStructure): TradingPremises =
    this.copy(businessStructure = Some(p), hasChanged = hasChanged || !this.businessStructure.contains(p))

  def agentName(p: AgentName): TradingPremises =
    this.copy(agentName = Some(p), hasChanged = hasChanged || !this.agentName.contains(p))

  def agentCompanyName(p: AgentCompanyName): TradingPremises =
    this.copy(agentCompanyName = Some(p), hasChanged = hasChanged || !this.agentCompanyName.contains(p))

  def agentPartnership(p: AgentPartnership): TradingPremises =
    this.copy(agentPartnership = Some(p), hasChanged = hasChanged || !this.agentPartnership.contains(p))

  def yourTradingPremises(p: YourTradingPremises): TradingPremises =
    this.copy(yourTradingPremises = Some(p), hasChanged = hasChanged || !this.yourTradingPremises.contains(p))

  def whatDoesYourBusinessDoAtThisAddress(p: WhatDoesYourBusinessDo): TradingPremises =
    this.copy(whatDoesYourBusinessDoAtThisAddress = Some(p), hasChanged = hasChanged || !this.whatDoesYourBusinessDoAtThisAddress.contains(p))

  def msbServices(p: MsbServices): TradingPremises =
    this.copy(msbServices = Some(p), hasChanged = hasChanged || !this.msbServices.contains(p))

  def registeringAgentPremises(p: RegisteringAgentPremises): TradingPremises =
    this.copy(registeringAgentPremises = Some(p), hasChanged = hasChanged || !this.registeringAgentPremises.contains(p))

  def isComplete: Boolean =
    this match {
      case TradingPremises(_,Some(x), _, _,_,_,Some(_),_,_,_,_,_) => true
      case TradingPremises(_,_,Some(_), Some(_),Some(_),Some(_), Some(_), _,_,_,_,_) => true
      case TradingPremises(None, None, None, None, None, None, None, None,_,_,_,_) => true //This code part of fix for the issue AMLS-1549 back button issue
      case _ => false
    }
}

object TradingPremises {

  import play.api.libs.json._

  val key = "trading-premises"

  def anyChanged(newModel: Seq[TradingPremises]): Boolean = {
    newModel exists { _.hasChanged }
  }

  def section(implicit cache: CacheMap): Section = {
    val messageKey = "tradingpremises"
    val notStarted = Section(messageKey, NotStarted, false, controllers.tradingpremises.routes.TradingPremisesAddController.get(true))

    cache.getEntry[Seq[TradingPremises]](key).fold(notStarted) {tp =>
      tp.filterNot(_.status.contains(StatusConstants.Deleted)).filterNot(_ == TradingPremises()) match {
        case Nil => Section(messageKey, NotStarted, anyChanged(tp), controllers.tradingpremises.routes.TradingPremisesAddController.get(true))
        case premises if premises.nonEmpty && premises.forall {
          _.isComplete
        } => Section(messageKey, Completed, anyChanged(tp), controllers.tradingpremises.routes.SummaryController.answers())
        case premises => {
          val index = premises.indexWhere {
            case model if !model.isComplete => true
            case _ => false
          }
          Section(messageKey, Started, anyChanged(tp), controllers.tradingpremises.routes.WhatYouNeedController.get(index + 1))
        }
      }
    }
  }

  implicit val mongoKey = new MongoKey[TradingPremises] {
    override def apply(): String = "trading-premises"
  }

  implicit val reads: Reads[TradingPremises] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._
    (
      (__ \ "registeringAgentPremises").readNullable[RegisteringAgentPremises] and
        (__ \ "yourTradingPremises").readNullable[YourTradingPremises] and
        (__ \ "businessStructure").readNullable[BusinessStructure] and
        (__ \ "agentName").readNullable[AgentName] and
        (__ \ "agentCompanyName").readNullable[AgentCompanyName] and
        (__ \ "agentPartnership").readNullable[AgentPartnership] and
        (__ \ "whatDoesYourBusinessDoAtThisAddress").readNullable[WhatDoesYourBusinessDo] and
        (__ \ "msbServices").readNullable[MsbServices] and
        (__ \ "hasChanged").readNullable[Boolean].map {_.getOrElse(false)} and
        (__ \ "lineId").readNullable[Int] and
        (__ \ "status").readNullable[String] and
        (__ \ "endDate").readNullable[ActivityEndDate]
      ) apply TradingPremises.apply _
  }

  implicit val writes: Writes[TradingPremises] = Json.writes[TradingPremises]

  implicit def default(tradingPremises: Option[TradingPremises]): TradingPremises =
    tradingPremises.getOrElse(TradingPremises())
}
