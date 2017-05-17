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

package audit

import uk.gov.hmrc.play.audit.AuditExtensions._
import models.responsiblepeople.{PersonAddressNonUK, PersonAddressUK}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.http.HeaderCarrier
import Utils.toMap
import play.api.libs.json.Json
import cats.implicits._
import models.aboutthebusiness.RegisteredOfficeUK
import models.tradingpremises.{Address => TradingPremisesAddress}

case class AuditAddress(addressLine1: String, addressLine2: String, addressLine3: Option[String], country: String, postCode: Option[String])

object AuditAddress {
  implicit val format = Json.format[AuditAddress]
}

object AddressCreatedEvent {

  def apply(address: AuditAddress)(implicit hc: HeaderCarrier) = DataEvent(
    auditSource = AppName.appName,
    auditType = "manualAddressSubmitted",
    tags = hc.toAuditTags("manualBankAccountSubmitted", "n/a"),
    detail = hc.toAuditDetails() ++ toMap(address)
  )

}

object AddressConversions {
  implicit def convert(address: PersonAddressUK): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postCode.some)

  implicit def convert(address: PersonAddressNonUK): AuditAddress =
    AuditAddress(address.addressLineNonUK1, address.addressLineNonUK2, address.addressLineNonUK3, address.country.name, None)

  implicit def convert(address: TradingPremisesAddress): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postcode.some)

  implicit def convert(address: RegisteredOfficeUK): AuditAddress =
    AuditAddress(address.addressLine1, address.addressLine2, address.addressLine3, "GB", address.postCode.some)
}
