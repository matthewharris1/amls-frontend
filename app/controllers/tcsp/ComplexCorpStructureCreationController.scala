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

package controllers.tcsp

import connectors.DataCacheConnector
import controllers.{AmlsBaseController, CommonPlayDependencies}
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.Inject
import models.tcsp._
import play.api.mvc.MessagesControllerComponents
import utils.AuthAction
import scala.concurrent.ExecutionContext.Implicits.global
import views.html.tcsp._

import scala.concurrent.Future

class ComplexCorpStructureCreationController @Inject()(val authAction: AuthAction,
                                                       val ds: CommonPlayDependencies,
                                                       val dataCacheConnector: DataCacheConnector,
                                                       val cc: MessagesControllerComponents) extends AmlsBaseController(ds, cc) {

  val NAME = "complexCorpStructureCreation"
  implicit val boolWrite = utils.BooleanFormReadWrite.formWrites(NAME)
  implicit val boolRead = utils.BooleanFormReadWrite.formRule(NAME, "error.required.tcsp.complex.corporate.structures")

  def get(edit: Boolean = false) = authAction.async {
    implicit request =>
      dataCacheConnector.fetch[Tcsp](request.credId, Tcsp.key) map {
        response =>
          val form: Form2[ComplexCorpStructureCreation] = (for {
            tcsp <- response
            model <- tcsp.complexCorpStructureCreation
          } yield Form2[ComplexCorpStructureCreation](model)) getOrElse EmptyForm
          Ok(complex_corp_structure_creation(form, edit))
      }
  }

  def post(edit: Boolean = false) = authAction.async {
    implicit request =>
      Form2[Boolean](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(complex_corp_structure_creation(f, edit)))
        case ValidForm(_, data) =>
          val res = data match {
            case true => ComplexCorpStructureCreationYes
            case false => ComplexCorpStructureCreationNo
          }
          for {
            tcsp <- dataCacheConnector.fetch[Tcsp](request.credId, Tcsp.key)
            _ <- dataCacheConnector.save[Tcsp](request.credId, Tcsp.key, tcsp.complexCorpStructureCreation(res))
          } yield redirectTo(edit, tcsp)
      }
  }

  def redirectTo(edit: Boolean, tcsp: Tcsp) = {
    (edit, tcsp.tcspTypes.map(t => t.serviceProviders.contains(RegisteredOfficeEtc))) match {
      case (_, Some(true)) => Redirect(routes.ProvidedServicesController.get(edit))
      case (false, Some(false)) => Redirect(routes.ServicesOfAnotherTCSPController.get(edit))
      case _ => Redirect(routes.SummaryController.get())
    }
  }
}