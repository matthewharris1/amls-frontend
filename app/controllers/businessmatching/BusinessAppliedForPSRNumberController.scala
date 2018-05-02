/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.businessmatching

import _root_.forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import models.businessmatching.{BusinessAppliedForPSRNumber, BusinessAppliedForPSRNumberNo, BusinessAppliedForPSRNumberYes, BusinessMatching}
import play.api.Play
import services.businessmatching.BusinessMatchingService
import views.html.businessmatching.business_applied_for_psr_number
import cats.data.OptionT
import cats.implicits._

import scala.concurrent.Future

trait BusinessAppliedForPSRNumberController extends BaseController {

  val dataCacheConnector: DataCacheConnector
  val businessMatchingService: BusinessMatchingService

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        businessMatchingService.getModel.value map { maybeBm =>
          val form: Form2[BusinessAppliedForPSRNumber] = (for {
            bm <- maybeBm
            number <- bm.businessAppliedForPSRNumber
          } yield Form2[BusinessAppliedForPSRNumber](number)).getOrElse(EmptyForm)
          Ok(business_applied_for_psr_number(form, edit, maybeBm.fold(false)(_.preAppComplete)))
        }
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request => {
        Form2[BusinessAppliedForPSRNumber](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(business_applied_for_psr_number(f, edit)))
          case ValidForm(_, BusinessAppliedForPSRNumberYes(x)) => {
            (for {
              bm <- businessMatchingService.getModel
              _ <- businessMatchingService.updateModel(
                bm.businessAppliedForPSRNumber(Some(BusinessAppliedForPSRNumberYes(x)))
              )
            } yield {
              Redirect(routes.SummaryController.get())
            }) getOrElse InternalServerError("Could not update psr number")
          }
          case ValidForm(_, _) =>
            Future.successful(Redirect(routes.NoPsrController.get()))
        }
      }
  }
}

object BusinessAppliedForPSRNumberController extends BusinessAppliedForPSRNumberController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
  override lazy val businessMatchingService = Play.current.injector.instanceOf[BusinessMatchingService]
}
