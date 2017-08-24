/*
 * Copyright 2017 HM Revenue & Customs
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

package models.responsiblepeople

import models.FormTypes._
import org.joda.time.LocalDate
import jto.validation._
import jto.validation.forms.Rules._
import jto.validation.forms.UrlFormEncoded
import jto.validation.ValidationError
import play.api.libs.json.{Json, Reads, Writes, _}
import cats.data.Validated.{Invalid, Valid}
import models.ValidationRule
import models.businessmatching.BusinessType
import utils.TraversableValidators._

import scala.collection.immutable.HashSet

case class Positions(positions: Set[PositionWithinBusiness], startDate: Option[LocalDate]) {

  def isNominatedOfficer = positions.contains(NominatedOfficer)

  def isComplete = positions.nonEmpty

  def personalTax = this.positions.exists(a => a == SoleProprietor || a == Partner)
}

sealed trait PositionWithinBusiness

case object BeneficialOwner extends PositionWithinBusiness

case object Director extends PositionWithinBusiness

case object InternalAccountant extends PositionWithinBusiness

case object NominatedOfficer extends PositionWithinBusiness

case object Partner extends PositionWithinBusiness

case object SoleProprietor extends PositionWithinBusiness

case object DesignatedMember extends PositionWithinBusiness

private case object OtherSelection extends PositionWithinBusiness

case class Other(value: String) extends PositionWithinBusiness

object PositionWithinBusiness {

  implicit val formRule = Rule[String, PositionWithinBusiness] {
    case "01" => Valid(BeneficialOwner)
    case "02" => Valid(Director)
    case "03" => Valid(InternalAccountant)
    case "04" => Valid(NominatedOfficer)
    case "05" => Valid(Partner)
    case "06" => Valid(SoleProprietor)
    case "07" => Valid(DesignatedMember)
    case "other" => Valid(OtherSelection)
    case _ =>
      Invalid(Seq((Path \ "positions") -> Seq(ValidationError("error.invalid"))))
  }

  implicit val formWrite = Write[PositionWithinBusiness, String] {
    case BeneficialOwner => "01"
    case Director => "02"
    case InternalAccountant => "03"
    case NominatedOfficer => "04"
    case Partner => "05"
    case SoleProprietor => "06"
    case DesignatedMember => "07"
    case Other(_) => "other"
  }

  implicit val jsonReads: Reads[PositionWithinBusiness] =
    Reads {
      case JsString("01") => JsSuccess(BeneficialOwner)
      case JsString("02") => JsSuccess(Director)
      case JsString("03") => JsSuccess(InternalAccountant)
      case JsString("04") => JsSuccess(NominatedOfficer)
      case JsString("05") => JsSuccess(Partner)
      case JsString("06") => JsSuccess(SoleProprietor)
      case JsString("07") => JsSuccess(DesignatedMember)
      case JsObject(m) if m.contains("other") => JsSuccess(Other(m("other").as[String]))
      case _ => JsError((JsPath \ "positions") -> play.api.data.validation.ValidationError("error.invalid"))
    }

  implicit val jsonWrites = Writes[PositionWithinBusiness] {
    case BeneficialOwner => JsString("01")
    case Director => JsString("02")
    case InternalAccountant => JsString("03")
    case NominatedOfficer => JsString("04")
    case Partner => JsString("05")
    case SoleProprietor => JsString("06")
    case DesignatedMember => JsString("07")
    case Other(value) => Json.obj("other" -> value)
  }

  val positionsPerBusinessType = {
    List(
      BusinessType.SoleProprietor -> List(NominatedOfficer, SoleProprietor),
      BusinessType.Partnership -> List(NominatedOfficer, Partner),
      BusinessType.LimitedCompany -> List(BeneficialOwner, Director, NominatedOfficer),
      BusinessType.UnincorporatedBody -> List(BeneficialOwner, Director, NominatedOfficer),
      BusinessType.LPrLLP -> List(NominatedOfficer, Partner, DesignatedMember)
    )
  }

}

object Positions {
  import utils.MappingUtils.Implicits._

  private val positionReader = minLengthR[Set[PositionWithinBusiness]](1).withMessage("error.required.positionWithinBusiness")

  private val positionValidator = Rule[(Set[PositionWithinBusiness], Option[String]), Set[PositionWithinBusiness]] {
    case (s, None) if s.contains(OtherSelection) =>
      Invalid(Seq((Path \ "otherPosition") -> Seq(ValidationError("responsiblepeople.position_within_business.other_position.othermissing"))))
    case (s, Some(other)) if s.contains(OtherSelection) => Valid(s.collect {
      case OtherSelection => Other(other)
      case x => x
    })
    case (s, _) => Valid(s)
  }

  implicit def formReads: Rule[UrlFormEncoded, Positions] =
    From[UrlFormEncoded] { __ =>
      (((__ \ "positions").read(positionReader) ~ (__ \ "otherPosition").read[Option[String]]).tupled.andThen(positionValidator) ~
        (__ \ "startDate").read(localDateFutureRule.map { x: LocalDate => Some(x) })) (Positions.apply)
    }

  implicit def formWrites
  (implicit
   w: Write[PositionWithinBusiness, String]
  ) = Write[Positions, UrlFormEncoded] { data =>
    Map("positions[]" -> data.positions.toSeq.map(w.writes)) ++ {
      data.startDate match {
        case Some(startDate) => localDateWrite.writes(startDate) map {
          case (key, value) =>
            s"startDate.$key" -> value
        }
        case _ => Nil
      }
    } ++ {
      data.positions.collectFirst {
        case Other(v) => "otherPosition" -> Seq(v)
      }
    }
  }

  implicit val formats = Json.format[Positions]
}
