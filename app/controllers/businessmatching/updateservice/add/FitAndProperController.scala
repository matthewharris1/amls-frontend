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

package controllers.businessmatching.updateservice.add

import cats.data.OptionT
import cats.implicits._
import config.AppConfig
import connectors.DataCacheConnector
import controllers.BaseController
import controllers.businessmatching.updateservice.UpdateServiceHelper
import controllers.businessmatching.updateservice.add.routes._
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.{Inject, Singleton}
import models.businessmatching.{BusinessActivities, BusinessActivity, MoneyServiceBusiness, TrustAndCompanyServices}
import models.flowmanagement.{AddServiceFlowModel, FitAndProperPageId, TradingPremisesPageId}
import models.responsiblepeople.ResponsiblePeople
import play.api.mvc.{Request, Result}
import services.StatusService
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.Router
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{BooleanFormReadWrite, RepeatingSection}
import views.html.businessmatching.updateservice.add._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FitAndProperController @Inject()(
                                        val authConnector: AuthConnector,
                                        implicit val dataCacheConnector: DataCacheConnector,
                                        val statusService: StatusService,
                                        val businessMatchingService: BusinessMatchingService,
                                        val helper: UpdateServiceHelper,
                                        val router: Router[AddServiceFlowModel]
                                        ) extends BaseController with RepeatingSection {

  val NAME = "passedFitAndProper"

  implicit val boolWrite = BooleanFormReadWrite.formWrites(NAME)
  implicit val boolRead = BooleanFormReadWrite.formRule(NAME, "error.businessmatching.updateservice.fitandproper")

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        getFormData map { case (model) =>
          val form = model.fitAndProper map { v => Form2(v) } getOrElse EmptyForm
          Ok(fit_and_proper(form, edit))
        } getOrElse InternalServerError("Unable to show the view")

  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        Form2[Boolean](request.body) match {
          case form: InvalidForm => getFormData map { case (_) =>
            BadRequest(fit_and_proper(form, edit))
          } getOrElse InternalServerError("Unable to show the view")

          case ValidForm(_, data) =>
            dataCacheConnector.update[AddServiceFlowModel](AddServiceFlowModel.key) { case Some(model) =>
              model.isfitAndProper(Some(data))
                .responsiblePeople(if(data) model.responsiblePeople else None)
            } flatMap { model =>
              router.getRoute(FitAndProperPageId, model.get, edit)
            }
        }
  }

  private def getFormData(implicit hc: HeaderCarrier, ac: AuthContext): OptionT[Future, (AddServiceFlowModel)] = for {
    model <- OptionT(dataCacheConnector.fetch[AddServiceFlowModel](AddServiceFlowModel.key))
  } yield (model)

}