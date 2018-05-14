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
import connectors.DataCacheConnector
import controllers.BaseController
import controllers.businessmatching.updateservice.UpdateServiceHelper
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import javax.inject.{Inject, Singleton}
import models.businessmatching.{BusinessActivities, BusinessActivity}
import models.flowmanagement.{AddServiceFlowModel, TradingPremisesPageId}
import services.StatusService
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.Router
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.BooleanFormReadWrite
import views.html.businessmatching.updateservice.add.trading_premises

import scala.concurrent.Future

@Singleton
class TradingPremisesController @Inject()(
                                           val authConnector: AuthConnector,
                                           implicit val dataCacheConnector: DataCacheConnector,
                                           val statusService: StatusService,
                                           val businessMatchingService: BusinessMatchingService,
                                           val helper: UpdateServiceHelper,
                                           val router: Router[AddServiceFlowModel]
                                         ) extends BaseController {

  val fieldName = "tradingPremisesNewActivities"
  implicit val boolWrite = BooleanFormReadWrite.formWrites(fieldName)
  implicit val boolRead = BooleanFormReadWrite.formRule(fieldName, "error.businessmatching.updateservice.tradingpremisesnewactivities")

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        getFormData map { case (model, activity) =>
          val form = model.areNewActivitiesAtTradingPremises map { v => Form2(v) } getOrElse EmptyForm
          Ok(trading_premises(form, edit, BusinessActivities.getValue(activity)))
        } getOrElse InternalServerError("Unable to show the view")
  }

  private def getFormData(implicit hc: HeaderCarrier, ac: AuthContext): OptionT[Future, (AddServiceFlowModel, BusinessActivity)] = for {
    model <- OptionT(dataCacheConnector.fetch[AddServiceFlowModel](AddServiceFlowModel.key))
    activity <- OptionT.fromOption[Future](model.activity)
  } yield (model, activity)

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        Form2[Boolean](request.body) match {
          case form: InvalidForm => getFormData map { case (_, activity) =>
            BadRequest(trading_premises(form, edit, BusinessActivities.getValue(activity)))
          } getOrElse InternalServerError("Unable to show the view")

          case ValidForm(_, data) =>
            dataCacheConnector.update[AddServiceFlowModel](AddServiceFlowModel.key) {
              case Some(model) =>model.isActivityAtTradingPremises(Some(data))
                .tradingPremisesActivities(if (data) model.tradingPremisesActivities else None)
            } flatMap {
              case Some(model) => router.getRoute(TradingPremisesPageId, model, edit)
              case _ => Future.successful(InternalServerError("Cannot retrieve data"))
            }
        }
  }

}