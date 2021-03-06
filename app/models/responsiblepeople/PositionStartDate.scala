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

package models.responsiblepeople

import jto.validation._
import jto.validation.forms.UrlFormEncoded
import models.FormTypes._
import org.joda.time.{DateTimeFieldType, LocalDate}

case class PositionStartDate(startDate: LocalDate)

object PositionStartDate {

  implicit val formRule: Rule[UrlFormEncoded, PositionStartDate] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    (__ \ "startDate").read(newAllowedPastAndFutureDateRule("error.rp.position.required.date",
      "error.rp.position.invalid.date.after.1900",
      "error.rp.position.invalid.date.future",
      "error.rp.position.invalid.date.not.real")) map PositionStartDate.apply
  }

  implicit def formWrites: Write[PositionStartDate, UrlFormEncoded] = Write {
    case PositionStartDate(date) =>
      Map(
        "startDate.day" -> Seq(date.get(DateTimeFieldType.dayOfMonth()).toString),
        "startDate.month" -> Seq(date.get(DateTimeFieldType.monthOfYear()).toString),
        "startDate.year" -> Seq(date.get(DateTimeFieldType.year()).toString))
  }
}
