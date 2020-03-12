/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.eab

import config.ApplicationConfig
import models.registrationprogress.{Completed, NotStarted, Section, Started}
import play.api.Play
import play.api.libs.json._
import play.api.mvc.Call
import typeclasses.MongoKey
import uk.gov.hmrc.http.cache.client.CacheMap
import models.estateagentbusiness._

case class Eab(data: JsObject = Json.obj(),
                     hasChanged: Boolean = false,
                     hasAccepted: Boolean = false) {

  /**
    * Provides a means of setting data that will update the hasChanged flag
    *
    * Set data via this method and NOT directly in the constructor
    */
  def data(p: JsObject): Eab =
    this.copy(data = p, hasChanged = hasChanged || this.data != p, hasAccepted = hasAccepted && this.data == p)

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(data).getOrElse(None)

  def valueAt(path: JsPath): String = {
    get[JsValue](path).getOrElse(Eab.notPresent).toString().toLowerCase()
  }

  def accept: Eab = this.copy(hasAccepted = true)

  def isComplete: Boolean =
    isServicesComplete &&
    isRedressSchemeComplete &&
    isProtectionSchemeComplete &&
    isEstateAgentActPenaltyComplete &&
    isProfessionalBodyPenaltyComplete &&
    hasAccepted

  private[eab] def isServicesComplete: Boolean = (data \ "eabServicesProvided").as[List[String]].nonEmpty

  private[eab]def isRedressSchemeComplete: Boolean = {
    val services = (data \ "eabServicesProvided").as[List[String]]
    val scheme = get[String](Eab.redressScheme)
    (services, scheme) match {
      case (x, _) if !x.contains("residential") => true
      case (_, Some(x)) if x.nonEmpty && x.contains("propertyRedressScheme") => true
      case (_, Some(x)) if x.nonEmpty && x.contains("propertyOmbudsman") => true
      case (_, Some(x)) if x.nonEmpty && x.contains("notRegistered") => true
      case _ => false
    }
  }

  private[eab] def isProtectionSchemeComplete: Boolean = {
    val services = (data \ "eabServicesProvided").as[List[String]]
    val scheme = get[Boolean](Eab.clientMoneyProtectionScheme)
    (services, scheme) match {
      case (x, _) if !x.contains("lettings") => true
      case (_, Some(_)) => true
      case _ => false
    }
  }

  private[eab] def booleanAndDetailComplete(boolOpt: Option[Boolean], detailOpt: Option[String]):Boolean =
    (boolOpt, detailOpt) match {
      case (Some(true), Some(detail)) if detail.nonEmpty => true
      case (Some(false), _) => true
      case _ => false
    }

  private[eab] def isEstateAgentActPenaltyComplete: Boolean =
    booleanAndDetailComplete(get[Boolean](Eab.penalisedEstateAgentsAct), get[String](Eab.penalisedEstateAgentsActDetail))

  private[eab] def isProfessionalBodyPenaltyComplete: Boolean =
    booleanAndDetailComplete(get[Boolean](Eab.penalisedProfessionalBody), get[String](Eab.penalisedProfessionalBodyDetail))

  def isInvalidRedressScheme: Boolean = {
    val scheme = get[String](Eab.redressScheme)
    scheme match {
      case Some(x) if x.nonEmpty && x.contains("other") => true
      case Some(x) if x.nonEmpty && x.contains("ombudsmanServices") => true
      case _ => false
    }
  }

  /* DES submission logic
  * Model to be used for DES submission such that the JSON be converted to objects that
  * can be parsed and understood by AMLS BE and associated models
  */
  def convServices: Set[Service] = {

    val newServices: Seq[String] = (data \ "eabServicesProvided").as[List[String]]

    newServices.map {
      case "residential"            => Residential
      case "commercial"             => Commercial
      case "auctioneering"          => Auction
      case "relocation"             => Relocation
      case "businessTransfer"       => BusinessTransfer
      case "assetManagement"        => AssetManagement
      case "landManagement"         => LandManagement
      case "developmentCompany"     => Development
      case "socialHousingProvision" => SocialHousing
      case "lettings"               => Lettings
    }.toSet
  }

  def convRedressScheme: Option[RedressScheme] = {

    val scheme = get[String](Eab.redressScheme)

    scheme match {
      case Some("propertyOmbudsman")     => Some(ThePropertyOmbudsman)
      case Some("ombudsmanServices")     => Some(OmbudsmanServices)
      case Some("propertyRedressScheme") => Some(PropertyRedressScheme)
      case Some("notRegistered")         => Some(RedressSchemedNo)
      case _                             => None
    }
  }

  def convPenalisedProfessionalBody: Option[ProfessionalBody] = {

    val penalisedProfessionalBody: Option[Boolean]      = get[Boolean](Eab.penalisedProfessionalBody)
    val penalisedProfessionalBodyDetail: Option[String] = get[String](Eab.penalisedProfessionalBodyDetail)

    penalisedProfessionalBody match {
      case Some(true)  => Some(ProfessionalBodyYes(penalisedProfessionalBodyDetail.getOrElse("")))
      case Some(false) => Some(ProfessionalBodyNo)
      case _           => None
    }
  }

  def convPenalisedUnderEstateAgentsAct: Option[PenalisedUnderEstateAgentsAct] = {

    val penalisedEstateAgentsAct: Option[Boolean]      = get[Boolean](Eab.penalisedEstateAgentsAct)
    val penalisedEstateAgentsActDetail: Option[String] = get[String](Eab.penalisedEstateAgentsActDetail)

    penalisedEstateAgentsAct match {
      case Some(true)  => Some(PenalisedUnderEstateAgentsActYes(penalisedEstateAgentsActDetail.getOrElse("")))
      case Some(false) => Some(PenalisedUnderEstateAgentsActNo)
      case _           => None
    }
  }

  def convClientMoneyProtectionScheme: Option[ClientMoneyProtectionScheme] = {

    val clientMoneyProtectionScheme = get[Boolean](Eab.clientMoneyProtectionScheme)

    clientMoneyProtectionScheme match {
      case Some(true)  => Some(ClientMoneyProtectionSchemeYes)
      case Some(false) => Some(ClientMoneyProtectionSchemeNo)
      case _           => None
    }
  }

  def estateAgentBusinessModel = {

    EstateAgentBusiness(
      services                      = Some(Services(convServices)),
      redressScheme                 = convRedressScheme,
      professionalBody              = convPenalisedProfessionalBody,
      penalisedUnderEstateAgentsAct = convPenalisedUnderEstateAgentsAct,
      clientMoneyProtectionScheme   = convClientMoneyProtectionScheme
    )

  }
  /* END DES submission logic */
}

object Eab {

  lazy val appConfig = Play.current.injector.instanceOf[ApplicationConfig]

  val eabServicesProvided             = JsPath \ "eabServicesProvided"
  val redressScheme                   = JsPath \ "redressScheme"
  val redressSchemeDetail             = JsPath \ "redressSchemeDetail"
  val clientMoneyProtectionScheme     = JsPath \ "clientMoneyProtectionScheme"
  val penalisedEstateAgentsAct        = JsPath \ "penalisedEstateAgentsAct"
  val penalisedEstateAgentsActDetail  = JsPath \ "penalisedEstateAgentsActDetail"
  val penalisedProfessionalBody       = JsPath \ "penalisedProfessionalBody"
  val penalisedProfessionalBodyDetail = JsPath \ "penalisedProfessionalBodyDetail"
  val notPresent                      = "null"

  val redirectCallType       = "GET"
  val key                    = "estate-agent-business"

  private def generateRedirect(destinationUrl: String) = {
    Call(redirectCallType, destinationUrl)
  }

  def section(implicit cache: CacheMap): Section = {
    val messageKey = "eab"
    val notStarted = Section(messageKey, NotStarted, false, generateRedirect(appConfig.eabWhatYouNeedUrl))
    cache.getEntry[Eab](key).fold(notStarted) {
      model =>
        if (model.isComplete && model.hasAccepted) {
          Section(messageKey, Completed, model.hasChanged, generateRedirect(appConfig.eabSummaryUrl))
        } else {
          Section(messageKey, Started, model.hasChanged, generateRedirect(appConfig.eabWhatYouNeedUrl))
        }
    }
  }

  implicit val mongoKey = new MongoKey[Eab] {
    override def apply(): String = key
  }

  implicit lazy val reads: Reads[Eab] = {

    val servicesTransform = (__ \ 'data ++ eabServicesProvided).json.copyFrom(
      (__ \ 'services).json.pick.map {
        case JsArray(values) => JsArray(values.map {
          case JsString("01") => JsString("residential")
          case JsString("02") => JsString("commercial")
          case JsString("03") => JsString("auctioneering")
          case JsString("04") => JsString("relocation")
          case JsString("05") => JsString("businessTransfer")
          case JsString("06") => JsString("assetManagement")
          case JsString("07") => JsString("landManagement")
          case JsString("08") => JsString("developmentCompany")
          case JsString("09") => JsString("socialHousingProvision")
          case JsString("10") => JsString("lettings")
          case _ => JsNull
        })
        case _ => JsArray()
      })

    val isRedressTransform = (__ \ 'data ++ redressScheme).json.copyFrom(
      (__ \ 'isRedress).readNullable[JsValue].map {
        case Some(JsBoolean(false)) => Some(JsString("notRegistered"))
        case _ => None
      }.filter(redressScheme => redressScheme.isDefined).orElse(
        (__ \ 'propertyRedressScheme).readNullable[JsValue].map {
          case Some(JsString("01")) => Some(JsString("propertyOmbudsman"))
          case Some(JsString("02")) => Some(JsString("ombudsmanServices"))
          case Some(JsString("03")) => Some(JsString("propertyRedressScheme"))
          case Some(JsString("04")) => Some(JsString("other"))
          case _ => None
        }
      ).map {
        case Some(redressScheme) => redressScheme
        case None => JsNull
      }
    )

    def readPathOrReturn(path: JsPath, returnValue: JsValue) =
      path.readNullable[JsValue].map(_.getOrElse(returnValue))

    import play.api.libs.functional.syntax._
    import play.api.libs.json.Reads._

    val oldModelTransformer:Reads[JsObject] = (servicesTransform and isRedressTransform and
      (__ \ 'data ++ penalisedEstateAgentsAct).json.copyFrom(readPathOrReturn(__ \ 'penalisedUnderEstateAgentsAct, JsNull)) and
      (__ \ 'data ++ penalisedEstateAgentsActDetail).json.copyFrom(readPathOrReturn( __ \ 'penalisedUnderEstateAgentsActDetails, JsNull)) and
      (__ \ 'data ++ penalisedProfessionalBody).json.copyFrom(readPathOrReturn(__ \ 'penalised, JsNull)) and
      (__ \ 'data ++ penalisedProfessionalBodyDetail).json.copyFrom(readPathOrReturn(__ \ 'professionalBody,JsNull)) and
      (__ \ 'hasAccepted).json.copyFrom((__ \ 'hasAccepted).json.pick) and
      (__ \ 'hasChanged).json.copyFrom((__ \ 'hasChanged).json.pick)
    ) reduce

    val jsonReads = (
        (__ \ "data").read[JsObject] and
        (__ \ "hasChanged").readNullable[Boolean].map(_.getOrElse(false)) and
        (__ \ "hasAccepted" ).readNullable[Boolean].map(_.getOrElse(false))
      )(Eab.apply _)

    (__ \ "services").readNullable[List[String]]
      .flatMap(_ => oldModelTransformer) andThen jsonReads orElse jsonReads
  }

  implicit lazy val writes: OWrites[Eab] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "data").write[JsObject] and
      (__ \ "hasChanged").write[Boolean] and
      (__ \ "hasAccepted").write[Boolean]
    ) (unlift(Eab.unapply))
  }

  implicit val formatOption = Reads.optionWithNull[Eab]
}


