package models.estateagentbusiness

import play.api.data.mapping.forms.UrlFormEncoded
import play.api.data.mapping._
import play.api.data.validation.ValidationError
import play.api.libs.json._

sealed trait Service

case object Commercial extends Service
case object Auction extends Service
case object Relocation extends Service
case object BusinessTransfer extends Service
case object AssetManagement extends Service
case object LandManagement extends Service
case object Development extends Service
case object SocialHousing extends Service
case class Residential(redressScheme: Option[RedressScheme]) extends Service

object Service {

  implicit def fromString(str : String, form:UrlFormEncoded) : Option[Service] = {
    str match {
      case "01" => Some(Commercial)
      case "02" => Some(Auction)
      case "03" => Some(Relocation)
      case "04" => Some(BusinessTransfer)
      case "05" => Some(AssetManagement)
      case "06" => Some(LandManagement)
      case "07" => Some(Development)
      case "08" => Some(SocialHousing)
      case "09" =>
      {
        val validateRedress = RedressScheme.formRedressRule.validate(form)
        Some(Residential(Some(validateRedress.get)))
      }
      case _ => None
    }
  }

  implicit def fromString(str : String) : Service = {
    str match {
      case "01" => Commercial
      case "02" => Auction
      case "03" => Relocation
      case "04" => BusinessTransfer
      case "05" => AssetManagement
      case "06" => LandManagement
      case "07" => Development
      case "08" => SocialHousing
      case "09" => {
        val test: Reads[RedressScheme] = __.read[RedressScheme] map (x => (x))
        test match {
          case s: JsSuccess[RedressScheme] => {
            val place: RedressScheme = s.get
            Residential(Some(place))
          }
          case e: JsError => Residential(None)
        }
      }
    }
  }

  implicit def servicesToString(obj : Service) : String = {
    obj match {
      case Commercial => "01"
      case Auction => "02"
      case Relocation => "03"
      case Auction => "04"
      case AssetManagement => "05"
      case LandManagement => "06"
      case Development => "07"
      case SocialHousing => "08"
      case Residential(None) => {
        __.write[RedressScheme]
      }
    }
  }

  implicit val servicesFormRule : Rule[UrlFormEncoded, Seq[Service]] = new Rule[UrlFormEncoded, Seq[Service]] {
    def validate(form : UrlFormEncoded) : Validation[(Path, Seq[ValidationError]), Seq[Service]] = {

      form.getOrElse("services", Nil)
          .foldLeft[(Seq[ValidationError], Seq[Service])](Nil, Nil)((results, next) => {
                    fromString(next,form)
                      .map(service => (results._1, results._2 :+ service))
                      .getOrElse((results._1 :+ ValidationError(s"Invalid Service Type String $next"), results _2))
          }) match {
        case (Nil, services) => Success(services)
        case (err, _) => Failure(Seq(Path \ "services" -> err))
      }
    }
  }

  implicit val formWrites: Write[Seq[Service], UrlFormEncoded]= Write {
    case services => Map("services" -> services.map(servicesToString))
  }

  implicit val jsonReads: Reads[Seq[Service]] = {
    import play.api.libs.json.Reads.StringReads
    (__ \ "services").read[Seq[String]].flatMap {
      x => Reads(i => JsSuccess(x.map(fromString)))
    }
  }

  implicit val jsonWrites = Writes[Seq[Service]] {
    case services => Json.obj("services" -> services.map(servicesToString))
    case _ => JsNull
  }
}


