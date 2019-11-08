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

package controllers.responsiblepeople.address

import audit.AddressConversions._
import audit.{AddressCreatedEvent, AddressModifiedEvent}
import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.DefaultBaseController
import controllers.responsiblepeople.routes
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.Country
import models.responsiblepeople._
import models.status.SubmissionStatus
import play.api.mvc.{AnyContent, Request}
import services.{AutoCompleteService, StatusService}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.{AuthAction, ControllerHelper, DateOfChangeHelper, RepeatingSection}
import views.html.responsiblepeople.address.{current_address, current_address_UK}

import scala.concurrent.Future

class CurrentAddressController @Inject ()(
                                            val dataCacheConnector: DataCacheConnector,
                                            auditConnector: AuditConnector,
                                            autoCompleteService: AutoCompleteService,
                                            statusService: StatusService,
                                            authAction: AuthAction
                                          ) extends RepeatingSection with DefaultBaseController with DateOfChangeHelper {

  def get(index: Int, edit: Boolean = false, flow: Option[String] = None) = authAction.async {
    implicit request =>
        getData[ResponsiblePerson](request.credId, index) map {
          case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,
          Some(ResponsiblePersonAddressHistory(Some(currentAddress),_,_)),_,_,_,_,_,_,_,_,_,_,_, _))
          => Ok(current_address(Form2[ResponsiblePersonCurrentAddress](currentAddress), edit, index, flow, personName.titleName))
          case Some(ResponsiblePerson(Some(personName),_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_))
          => Ok(current_address(EmptyForm, edit, index, flow, personName.titleName))
          case _
          => NotFound(notFoundView)
        }
    }

  def post(index: Int, edit: Boolean = false, flow: Option[String] = None) =
    authAction.async {
        implicit request =>
          def processForm(data: ResponsiblePersonCurrentAddress) = {
            data.personAddress match {
              case _: PersonAddressUK => Future.successful(Redirect(routes.CurrentAddressUKController.get(index, edit, flow)))
              case _: PersonAddressNonUK => Future.successful(Redirect(routes.CurrentAddressNonUKController.get(index, edit, flow)))
            }
          }

          (Form2[ResponsiblePersonCurrentAddress](request.body) match {
            case f: InvalidForm if f.data.get("isUK").isDefined => processForm(ResponsiblePersonCurrentAddress(processAsValid(f), None, None))
            case f: InvalidForm =>
              getData[ResponsiblePerson](request.credId, index) map { rp =>
                BadRequest(current_address(f, edit, index, flow, ControllerHelper.rpTitleName(rp)))
              }
            case ValidForm(_, data) => {
              processForm(data)
          }}).recoverWith {
            case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
          }
    }

  //todo: populate the models correctly
  def processAsValid(f: InvalidForm): PersonAddress = {
    if(f.data.get("isUK").contains(Seq("true"))){
      PersonAddressUK("", "", None, None, "")
    } else {
      PersonAddressNonUK("", "", None, None, Country("", ""))
    }
  }
}