package models.supervision

import jto.validation._
import jto.validation.forms.Rules._
import jto.validation.forms.UrlFormEncoded
import play.api.libs.json._
import cats.data.Validated.{Invalid, Valid}

sealed trait ProfessionalBody

case class ProfessionalBodyYes(value : String) extends ProfessionalBody
case object ProfessionalBodyNo extends ProfessionalBody


object ProfessionalBody {

  import utils.MappingUtils.Implicits._

  val maxPenalisedTypeLength = 255
  val penalisedType = notEmpty.withMessage("error.required.eab.info.about.penalty") andThen
    maxLength(maxPenalisedTypeLength).withMessage("error.invalid.eab.info.about.penalty")

  implicit val formRule: Rule[UrlFormEncoded, ProfessionalBody] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    (__ \ "penalised").read[Boolean].withMessage("error.required.eab.penalised.by.professional.body") flatMap {
      case true =>
        (__ \ "professionalBody").read(penalisedType) map ProfessionalBodyYes.apply
      case false => Rule.fromMapping { _ => Valid(ProfessionalBodyNo) }
    }
  }

  implicit val formWrites: Write[ProfessionalBody, UrlFormEncoded] = Write {
    case ProfessionalBodyYes(value) =>
      Map("penalised" -> Seq("true"),
        "professionalBody" -> Seq(value)
      )
    case ProfessionalBodyNo => Map("penalised" -> Seq("false"))
  }

  implicit val jsonReads: Reads[ProfessionalBody] =
    (__ \ "penalised").read[Boolean] flatMap {
    case true => (__ \ "professionalBody").read[String] map ProfessionalBodyYes.apply _
    case false => Reads(_ => JsSuccess(ProfessionalBodyNo))
  }

  implicit val jsonWrites = Writes[ProfessionalBody] {
    case ProfessionalBodyYes(value) => Json.obj(
      "penalised" -> true,
      "professionalBody" -> value
    )
    case ProfessionalBodyNo => Json.obj("penalised" -> false)
  }
}
