/*
 * Copyright 2019 HM Revenue & Customs
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

package models.renewal

import play.api.libs.json.{Json, Reads, Writes, __}

case class CashPayments(cashPaymentsCustomerNotMet: CashPaymentsCustomerNotMet, howCashPaymentsReceived: Option[HowCashPaymentsReceived])

object CashPayments {

  implicit val jsonReads: Reads[CashPayments] = {
    import play.api.libs.functional.syntax._
    ((__ \ "receivePayments").read[Boolean] map CashPaymentsCustomerNotMet.apply and
      (__ \ "paymentMethods").readNullable[PaymentMethods].map {
        case Some(methods) => Some(HowCashPaymentsReceived.apply(methods))
        case _ => None
      })(CashPayments.apply _ )
  }

  implicit val jsonWrites: Writes[CashPayments] = {
    Writes[CashPayments] {
        case CashPayments(CashPaymentsCustomerNotMet(true), Some(HowCashPaymentsReceived(cp))) =>
          val maybeOther = if(cp.other.isDefined) {
            Json.obj("details" -> cp.other)
          } else {
            Json.obj()
          }

          val payMet = Json.obj(
            "courier" -> cp.courier,
            "direct" -> cp.direct,
            "other" -> cp.other.isDefined
          ) ++ maybeOther

         Json.obj("receivePayments" -> true,
          "paymentMethods" -> payMet)
        case CashPayments(CashPaymentsCustomerNotMet(receivePayments), _) => Json.obj("receivePayments" -> receivePayments)
      }
  }

  implicit def convert(model: CashPayments): Option[models.hvd.PaymentMethods] = model.howCashPaymentsReceived map { pm =>
    models.hvd.PaymentMethods(pm.paymentMethods.courier, pm.paymentMethods.direct, pm.paymentMethods.other)
  }
}