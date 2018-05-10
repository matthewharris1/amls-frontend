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

package controllers.businessmatching.updateservice

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import javax.inject.{Inject, Singleton}
import models.businessmatching.{MoneyServiceBusiness, BusinessActivities => BMBusinessActivities, BusinessActivity => BMBusinessActivity, BusinessMatching => BMBusinessMatching}
import models.flowmanagement.RemoveServiceFlowModel
import models.tradingpremises.{TradingPremises, WhatDoesYourBusinessDo}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RemoveServiceHelper @Inject()(val authConnector: AuthConnector,
                                    implicit val dataCacheConnector: DataCacheConnector
                                   ) {


  def removeBusinessMatchingBusinessActivities(model: RemoveServiceFlowModel)(implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext): OptionT[Future, BMBusinessMatching] = {

    for {
      activitiesToRemove <- OptionT.fromOption[Future](model.activitiesToRemove)
      currentBusinessMatching <- OptionT(dataCacheConnector.fetch[BMBusinessMatching](BMBusinessMatching.key))
      currentActivities <- OptionT.fromOption[Future](currentBusinessMatching.activities) orElse OptionT.some[Future, BMBusinessActivities](BMBusinessActivities(Set.empty[BMBusinessActivity]))

      newBusinessMatching <- {

        OptionT(dataCacheConnector.update[BMBusinessMatching](BMBusinessMatching.key) {
          case Some(bm) if activitiesToRemove.contains(MoneyServiceBusiness)  =>
            bm.activities(currentActivities.copy(businessActivities = currentActivities.businessActivities - MoneyServiceBusiness))
              .msbServices(None)
              .businessAppliedForPSRNumber(None)
              .copy(hasAccepted = true)
          }
        )
      }
    } yield newBusinessMatching

  }

  def removeTradingPremisesBusinessActivities(model: RemoveServiceFlowModel)(implicit ac: AuthContext, hc: HeaderCarrier, ec: ExecutionContext): OptionT[Future, TradingPremises] = {

    for {
      activitiesToRemove <- OptionT.fromOption[Future](model.activitiesToRemove)
      currentTradingPremises <- OptionT(dataCacheConnector.fetch[TradingPremises](TradingPremises.key))
      currentActivities <- OptionT.fromOption[Future](currentTradingPremises.whatDoesYourBusinessDoAtThisAddress) orElse OptionT.some[Future, WhatDoesYourBusinessDo](WhatDoesYourBusinessDo(Set.empty[BMBusinessActivity]))

      newBusinessMatching <- {

        OptionT(dataCacheConnector.update[TradingPremises](TradingPremises.key) {
          case Some(tp) if activitiesToRemove.contains(MoneyServiceBusiness)  =>
            tp.whatDoesYourBusinessDoAtThisAddress(currentActivities.copy(activities = currentActivities.activities - MoneyServiceBusiness))
              .msbServices(None)
              .copy(hasAccepted = true)
        }
        )
      }
    } yield newBusinessMatching

  }
}
