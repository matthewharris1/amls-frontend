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

package controllers.businessdetails

import audit.AddressConversions._
import audit.{AddressCreatedEvent, AddressModifiedEvent}
import cats.data.OptionT
import cats.implicits._
import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.{AmlsBaseController, CommonPlayDependencies}
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.businessdetails.{BusinessDetails, CorrespondenceAddress, CorrespondenceAddressNonUk}
import play.api.mvc.{MessagesControllerComponents, Request}
import services.AutoCompleteService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import utils.AuthAction
import scala.concurrent.ExecutionContext.Implicits.global
import views.html.businessdetails._

import scala.concurrent.Future

class CorrespondenceAddressNonUkController @Inject ()(val dataConnector: DataCacheConnector,
                                                      val auditConnector: AuditConnector,
                                                      val autoCompleteService: AutoCompleteService,
                                                      val authAction: AuthAction,
                                                      val ds: CommonPlayDependencies,
                                                      val cc: MessagesControllerComponents) extends AmlsBaseController(ds, cc) {

  def get(edit: Boolean = false) = authAction.async {
    implicit request =>
      dataConnector.fetch[BusinessDetails](request.credId, BusinessDetails.key) map {
        response =>
          val form: Form2[CorrespondenceAddressNonUk] = (for {
            businessDetails <- response
            correspondenceAddress <- businessDetails.correspondenceAddress
            ukAddress <- correspondenceAddress.nonUkAddress
          } yield Form2[CorrespondenceAddressNonUk](ukAddress)).getOrElse(EmptyForm)
          Ok(correspondence_address_non_uk(form, edit, autoCompleteService.getCountries))
      }
  }

  def post(edit: Boolean = false) = authAction.async {
    implicit request => {
      Form2[CorrespondenceAddressNonUk](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(correspondence_address_non_uk(f, edit, autoCompleteService.getCountries)))
        case ValidForm(_, data) =>
          val doUpdate = for {
            businessDetails:BusinessDetails <- OptionT(dataConnector.fetch[BusinessDetails](request.credId, BusinessDetails.key))
            _ <- OptionT.liftF(dataConnector.save[BusinessDetails]
              (request.credId, BusinessDetails.key, businessDetails.correspondenceAddress(CorrespondenceAddress(None, Some(data)))))
            _ <- OptionT.liftF(auditAddressChange(data, businessDetails.correspondenceAddress.flatMap(a => a.nonUkAddress), edit)) orElse OptionT.some(Success)
          } yield Redirect(routes.SummaryController.get())
          doUpdate getOrElse InternalServerError("Could not update correspondence address")
      }
    }
  }

  def auditAddressChange(currentAddress: CorrespondenceAddressNonUk, oldAddress: Option[CorrespondenceAddressNonUk], edit: Boolean)
                        (implicit hc: HeaderCarrier, request: Request[_]): Future[AuditResult] = {
    if (edit) {
      auditConnector.sendEvent(AddressModifiedEvent(currentAddress, oldAddress))
    } else {
      auditConnector.sendEvent(AddressCreatedEvent(currentAddress))
    }
  }
}