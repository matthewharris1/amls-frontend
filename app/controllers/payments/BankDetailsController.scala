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

package controllers.payments

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import controllers.{AmlsBaseController, CommonPlayDependencies}
import javax.inject.Inject
import models.SubmissionRequestStatus
import play.api.mvc.MessagesControllerComponents
import services.{AuthEnrolmentsService, FeeResponseService, RenewalService, StatusService}
import utils.{AuthAction, DeclarationHelper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BankDetailsController @Inject()(val dataCacheConnector: DataCacheConnector,
                                      val authAction: AuthAction,
                                      val ds: CommonPlayDependencies,
                                      val authEnrolmentsService: AuthEnrolmentsService,
                                      val feeResponseService: FeeResponseService,
                                      val statusService: StatusService,
                                      val cc: MessagesControllerComponents,
                                      val renewalService: RenewalService) extends AmlsBaseController(ds, cc) {


  def get(isUK: Boolean = true) = authAction.async {
    implicit request =>
      (for {
        submissionRequestStatus <- OptionT.liftF(dataCacheConnector.fetch[SubmissionRequestStatus](request.credId, SubmissionRequestStatus.key))
        status <- OptionT.liftF(statusService.getStatus(request.amlsRefNumber, request.accountTypeId, request.credId))
        amlsRegistrationNumber <- OptionT(authEnrolmentsService.amlsRegistrationNumber(request.amlsRefNumber, request.groupIdentifier))
        fees <- OptionT(feeResponseService.getFeeResponse(amlsRegistrationNumber, request.accountTypeId))
        subHeading <- DeclarationHelper.getSubheadingBasedOnStatus(request.credId, request.amlsRefNumber, request.accountTypeId, statusService, renewalService)
        paymentReference <- OptionT.fromOption[Future](fees.paymentReference)
      } yield {
        val amount = fees.toPay(status, submissionRequestStatus)
          Ok(views.html.payments.bank_details(isUK, amount, paymentReference, subHeading))
      }) getOrElse InternalServerError("Failed to retrieve submission data")
  }
}