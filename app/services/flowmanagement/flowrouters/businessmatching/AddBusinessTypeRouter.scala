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

package services.flowmanagement.flowrouters.businessmatching

import javax.inject.{Inject, Singleton}
import models.flowmanagement._
import play.api.mvc.Result
import services.businessmatching.BusinessMatchingService
import services.flowmanagement.Router
import services.flowmanagement.pagerouters.addflow._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddBusinessTypeRouter @Inject()(val businessMatchingService: BusinessMatchingService,
                                      val addMoreActivitiesPageRouter: AddMoreBusinessTypesPageRouter,
                                      val businessAppliedForPSRNumberPageRouter: BusinessAppliedForPsrNumberPageRouter,
                                      val fitAndProperPageRouter: FitAndProperPageRouter,
                                      val newServicesInformationPageRouter: NeedMoreInformationPageRouter,
                                      val noPSRPageRouter: NoPSRPageRouter,
                                      val selectActivitiesPageRouter: SelectBusinessTypesPageRouter,
                                      val subServicesPageRouter: SubSectorsPageRouter,
                                      val tradingPremisesPageRouter: TradingPremisesPageRouter,
                                      val updateServicesSummaryPageRouter: AddBusinessTypeSummaryPageRouter,
                                      val whatDoYouDoHerePageRouter: WhatDoYouDoHerePageRouter,
                                      val whichFitAndProperPageRouter: WhichFitAndProperPageRouter,
                                      val whichTradingPremisesPageRouter: WhichTradingPremisesPageRouter
                                     ) extends Router[AddBusinessTypeFlowModel] {


  override def getRoute(pageId: PageId, model: AddBusinessTypeFlowModel, edit: Boolean = false)
                       (implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {
    pageId match {
      case AddMoreBusinessTypesPageId => addMoreActivitiesPageRouter.getPageRoute(model, edit)
      case PsrNumberPageId => businessAppliedForPSRNumberPageRouter.getPageRoute(model, edit)
      case FitAndProperPageId => fitAndProperPageRouter.getPageRoute(model, edit)
      case NeedMoreInformationPageId => newServicesInformationPageRouter.getPageRoute(model, edit)
      case NoPSRPageId => noPSRPageRouter.getPageRoute(model, edit)
      case SelectBusinessTypesPageId => selectActivitiesPageRouter.getPageRoute(model, edit)
      case SubSectorsPageId => subServicesPageRouter.getPageRoute(model, edit)
      case TradingPremisesPageId => tradingPremisesPageRouter.getPageRoute(model, edit)
      case AddBusinessTypeSummaryPageId => updateServicesSummaryPageRouter.getPageRoute(model, edit)
      case WhatDoYouDoHerePageId => whatDoYouDoHerePageRouter.getPageRoute(model, edit)
      case WhichFitAndProperPageId => whichFitAndProperPageRouter.getPageRoute(model, edit)
      case WhichTradingPremisesPageId => whichTradingPremisesPageRouter.getPageRoute(model, edit)
    }
  }
}