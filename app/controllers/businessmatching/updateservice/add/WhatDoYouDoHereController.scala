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

import connectors.DataCacheConnector
import controllers.BaseController
import controllers.businessmatching.updateservice.UpdateServiceHelper
import forms.{EmptyForm, Form2}
import javax.inject.{Inject, Singleton}
import models.businessmatching.MsbServices
import models.flowmanagement._
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.Router
import services.{ResponsiblePeopleService, StatusService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.RepeatingSection

@Singleton
class WhatDoYouDoHereController @Inject()(
                                             val authConnector: AuthConnector,
                                             implicit val dataCacheConnector: DataCacheConnector,
                                             val statusService: StatusService,
                                             val businessMatchingService: BusinessMatchingService,
                                             val responsiblePeopleService: ResponsiblePeopleService,
                                             val helper: UpdateServiceHelper,
                                             val router: Router[AddServiceFlowModel]
                                           ) extends BaseController with RepeatingSection {

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        businessMatchingService.getModel.value map { maybeBM =>
          val form = (for {
            bm <- maybeBM
            services <- bm.msbServices
          } yield Form2[MsbServices](services)).getOrElse(EmptyForm)

          Ok(views.html.businessmatching.updateservice.add.what_do_you_do_here(form, edit, maybeBM.fold(false)(_.preAppComplete)))
        }
  }

  //hasAlreadyPassedFitAndProper
  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request => ???
//        Form2[ResponsiblePeopleFitAndProper](request.body) match {
//          case f: InvalidForm => responsiblePeopleService.getAll map { rp =>
//            BadRequest(which_fit_and_proper(f, edit, rp.zipWithIndex.exceptInactive))
//          }
//          case ValidForm(_, data) => {
//            dataCacheConnector.update[AddServiceFlowModel](AddServiceFlowModel.key) {
//              case Some(model) => {
//               model.responsiblePeople(Some(data))
//              }
//            } flatMap {
//              case Some(model) => {
//                router.getRoute(WhichFitAndProperPageId, model, edit)
//              }
//              case _ => Future.successful(InternalServerError("Cannot retrieve data"))
//            }
//          }
//          case _ => Future.successful(InternalServerError("Cannot retrieve form data"))
//        }
  }
}

