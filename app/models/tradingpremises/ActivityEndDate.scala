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

package models.tradingpremises

import models.FormTypes._
import org.joda.time.{DateTimeFieldType, LocalDate}
import jto.validation.forms._
import jto.validation.{From, Rule, Write}
import play.api.libs.json.Json

case class ActivityEndDate (endDate: LocalDate)

object ActivityEndDate {

  implicit val format =  Json.format[ActivityEndDate]

  implicit val formRule: Rule[UrlFormEncoded, ActivityEndDate] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    __.read(premisesEndDateRule) map ActivityEndDate.apply
  }

  implicit val formWrites: Write[ActivityEndDate, UrlFormEncoded] =
    Write {
      case ActivityEndDate(b) =>Map(
        "endDate.day" -> Seq(b.get(DateTimeFieldType.dayOfMonth()).toString),
        "endDate.month" -> Seq(b.get(DateTimeFieldType.monthOfYear()).toString),
        "endDate.year" -> Seq(b.get(DateTimeFieldType.year()).toString)
      )
    }
}
