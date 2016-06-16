package models.moneyservicebusiness

import models.Country
import play.api.data.mapping._
import play.api.data.mapping.forms.UrlFormEncoded
import play.api.libs.json.{Reads, Writes}
import utils.TraversableValidators

case class MostTransactions(countries: Seq[Country])

private sealed trait MostTransactions0 {

  private implicit def rule[A]
  (implicit
   a: Path => RuleLike[A, Seq[String]],
   cR: Rule[Seq[String], Seq[Country]]
  ): Rule[A, MostTransactions] =
    From[A] { __ =>

      import utils.MappingUtils.Implicits.RichRule
      import TraversableValidators._

      implicit val emptyToNone: String => Option[String] = {
        case "" => None
        case s => Some(s)
      }

      val seqR = {
        (seqToOptionSeq[String] compose flattenR[String] compose cR)
          .compose(minLengthR[Seq[Country]](1) withMessage "error.required.countries.msb.most.transactions")
          .compose(maxLengthR[Seq[Country]](3))
      }

      (__ \ "mostTransactionsCountries").read(seqR) fmap MostTransactions.apply
    }

  private implicit def write[A]
  (implicit
   a: Path => WriteLike[Seq[Country], A]
  ): Write[MostTransactions, A] =
    To[A] { __ =>

      import play.api.libs.functional.syntax.unlift
      (__ \ "mostTransactionsCountries").write[Seq[Country]] contramap unlift(MostTransactions.unapply)
    }

  val formR: Rule[UrlFormEncoded, MostTransactions] = {
    import play.api.data.mapping.forms.Rules._
    implicitly
  }

  val jsonR: Reads[MostTransactions] = {
    import play.api.data.mapping.json.Rules.{JsValue => _, pickInJson => _, _}
    import utils.JsonMapping._
    implicitly
  }

  val formW: Write[MostTransactions, UrlFormEncoded] = {
    import play.api.data.mapping.forms.Writes._
    implicitly
  }

  val jsonW: Writes[MostTransactions] = {
    import play.api.data.mapping.json.Writes._
    import utils.JsonMapping._
    implicitly
  }
}

object MostTransactions {

  private object Cache extends MostTransactions0

  implicit val formR: Rule[UrlFormEncoded, MostTransactions] = Cache.formR
  implicit val formW: Write[MostTransactions, UrlFormEncoded] = Cache.formW
  implicit val jsonR: Reads[MostTransactions] = Cache.jsonR
  implicit val jsonW: Writes[MostTransactions] = Cache.jsonW
}