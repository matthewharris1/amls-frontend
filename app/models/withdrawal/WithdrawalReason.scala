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

package models.withdrawal

import jto.validation.{From, Path, Rule, ValidationError}
import jto.validation.forms.UrlFormEncoded

sealed trait WithdrawalReason

object WithdrawalReason {

  case object OutOfScope extends WithdrawalReason
  case object NotTradingInOwnRight extends WithdrawalReason
  case object UnderAnotherSupervisor extends WithdrawalReason
  case object JoinedAWRSGroup extends WithdrawalReason
  case class Other(otherReason: String) extends WithdrawalReason

  import utils.MappingUtils.Implicits._

  implicit val formRule: Rule[UrlFormEncoded, WithdrawalReason] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    import models.FormTypes._
    (__ \ "withdrawalReason").read[String].withMessage("") flatMap {
      case "01" => OutOfScope
      case "02" => NotTradingInOwnRight
      case "03" => UnderAnotherSupervisor
      case "04" => JoinedAWRSGroup
      case "05" => (__ \ "specifyOtherReason").read[String] map Other.apply
      case _ =>
        (Path \ "withdrawalReason") -> Seq(ValidationError("error.invalid"))
    }
  }

}
