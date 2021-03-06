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

package models.businessmatching

import jto.validation.forms.UrlFormEncoded
import jto.validation.{From, Rule, Write}
import models.FormTypes._
import play.api.libs.json.Json

case class ConfirmPostcode (postCode: String)

object ConfirmPostcode {

  implicit val format = Json.format[ConfirmPostcode]

  implicit val formReads: Rule[UrlFormEncoded, ConfirmPostcode] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    (__ \ "postCode").read(postcodeTypeWithMsg("businessmatching.confirm.postcode.error.empty")) map ConfirmPostcode.apply
  }

  implicit val formWrites: Write[ConfirmPostcode, UrlFormEncoded] = Write {
    case ConfirmPostcode(postcode) => Map("postCode" -> Seq(postcode))
  }
}
