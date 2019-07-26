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

package controllers.declaration

import javax.inject.{Inject, Singleton}
import connectors.DataCacheConnector
import controllers.{BaseController, DefaultBaseController}
import models.status.{ReadyForRenewal, SubmissionDecisionApproved, SubmissionReadyForReview}
import services.StatusService
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.AuthAction

import scala.concurrent.Future


@Singleton
class RegisterResponsiblePersonController @Inject()(
                                                     val dataCacheConnector: DataCacheConnector,
                                                     authAction: AuthAction,
                                                     val statusService: StatusService
                                                   ) extends DefaultBaseController {

  def get() = authAction.async {
    implicit request => {

      statusService.getStatus(request.amlsRefNumber, request.accountTypeId, request.cacheId) map {
        case ReadyForRenewal(_) |
             SubmissionDecisionApproved |
             SubmissionReadyForReview => Ok(views.html.declaration.register_responsible_person("submit.amendment.application"))
        case _ => Ok(views.html.declaration.register_responsible_person("submit.registration"))
      }
    }
  }
}
