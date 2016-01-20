package models.aboutthebusiness

import models.FormTypes._
import play.api.data.mapping.forms._
import play.api.data.mapping._
import play.api.libs.json.{Writes, Reads, Json}

sealed trait RegisteredOffice {

  def toLines: Seq[String] = this match {
    case a: RegisteredOfficeUK =>
      Seq(
        Some(a.addressLine1),
        Some(a.addressLine2),
        a.addressLine3,
        a.addressLine4,
        Some(a.postCode)
      ).flatten
    case a: RegisteredOfficeNonUK =>
      Seq(
        Some(a.addressLine1),
        Some(a.addressLine2),
        a.addressLine3,
        a.addressLine4,
        Some(a.country)
      ).flatten
  }
}

case class RegisteredOfficeUK(
                               addressLine1: String,
                               addressLine2: String,
                               addressLine3: Option[String] = None,
                               addressLine4: Option[String] = None,
                               postCode: String
                             ) extends RegisteredOffice

case class RegisteredOfficeNonUK(
                                  addressLine1: String,
                                  addressLine2: String,
                                  addressLine3: Option[String] = None,
                                  addressLine4: Option[String] = None,
                                  country: String
                                ) extends RegisteredOffice

object RegisteredOffice {

  import utils.MappingUtils.Implicits._

  implicit val formRule: Rule[UrlFormEncoded, RegisteredOffice] = From[UrlFormEncoded] { __ =>
    import play.api.data.mapping.forms.Rules._
    (__ \ "isUK").read[Boolean] flatMap[RegisteredOffice] {
      case true =>
        (
          (__ \ "addressLine1").read(addressType) and
            (__ \ "addressLine2").read(addressType) and
            (__ \ "addressLine3").read(optionR(addressType)) and
            (__ \ "addressLine4").read(optionR(addressType)) and
            (__ \ "postCode").read(postCodeType)
          ) (RegisteredOfficeUK.apply _)
      case false =>
        (
          (__ \ "addressLine1").read(addressType) and
            (__ \ "addressLine2").read(addressType) and
            (__ \ "addressLine3").read(optionR(addressType)) and
            (__ \ "addressLine4").read(optionR(addressType)) and
            (__ \ "country").read(countryType)
          )(RegisteredOfficeNonUK.apply _)
    }
  }

  implicit val formWrites: Write[RegisteredOffice, UrlFormEncoded] = Write {
    case f: RegisteredOfficeUK =>
      Map(
        "isUK" -> Seq("true"),
        "addressLine1" -> f.addressLine1,
        "addressLine2" -> f.addressLine2,
        "addressLine3" -> Seq(f.addressLine3.getOrElse("")),
        "addressLine4" -> Seq(f.addressLine4.getOrElse("")),
        "postCode" -> f.postCode
      )
    case f: RegisteredOfficeNonUK =>
      Map(
        "isUK" -> Seq("false"),
        "addressLine1" -> f.addressLine1,
        "addressLine2" -> f.addressLine2,
        "addressLine3" -> Seq(f.addressLine3.getOrElse("")),
        "addressLine4" -> Seq(f.addressLine4.getOrElse("")),
        "country" -> f.country
      )
  }

  implicit val jsonReads: Reads[RegisteredOffice] = {
    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    (
      (__ \ "postCode").read[String] andKeep
        (
          (__ \ "addressLine1").read[String] and
            (__ \ "addressLine2").read[String] and
            (__ \ "addressLine3").read[Option[String]] and
            (__ \ "addressLine4").read[Option[String]] and
            (__ \ "postCode").read[String]
          ) (RegisteredOfficeUK.apply _) map identity[RegisteredOffice]
      ) orElse
      (
        (__ \ "addressLine1").read[String] and
          (__ \ "addressLine2").read[String] and
          (__ \ "addressLine3").read[Option[String]] and
          (__ \ "addressLine4").read[Option[String]] and
          (__ \ "country").read[String]
        ) (RegisteredOfficeNonUK.apply _)
  }

  implicit val jsonWrites = Writes[RegisteredOffice] {
    case m: RegisteredOfficeUK =>
      Json.obj("isUK" -> true,
        "addressLine1" -> m.addressLine1,
        "addressLine2" -> m.addressLine2,
        "addressLine3" -> m.addressLine3,
        "addressLine4" -> m.addressLine4,
        "postCode" -> m.postCode)
    case m: RegisteredOfficeNonUK =>
      Json.obj("isUK" -> false,
        "addressLine1" -> m.addressLine1,
        "addressLine2" -> m.addressLine2,
        "addressLine3" -> m.addressLine3,
        "addressLine4" -> m.addressLine4,
        "country" -> m.country)
  }
}