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

package controllers.declaration

import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.{AmlsBaseController, CommonPlayDependencies}
import models.declaration.AddPerson
import models.status.{ReadyForRenewal, RenewalSubmitted, SubmissionDecisionApproved, SubmissionReadyForReview}
import play.api.mvc.{MessagesControllerComponents, Result}
import services.{RenewalService, SectionsProvider, StatusService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{AuthAction, DeclarationHelper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs

class DeclarationController @Inject () (val dataCacheConnector: DataCacheConnector,
                                        val statusService: StatusService,
                                        authAction: AuthAction,
                                        val ds: CommonPlayDependencies,
                                        val cc: MessagesControllerComponents,
                                        val sectionsProvider: SectionsProvider,
                                        val renewalService: RenewalService) extends AmlsBaseController(ds, cc) {

  lazy val defaultView = declarationView("declaration.declaration.title", "submit.registration", isAmendment = false)

  def get() = authAction.async {
    implicit request => {
      DeclarationHelper.sectionsComplete(request.credId, sectionsProvider) flatMap {
        case true => dataCacheConnector.fetch[AddPerson](request.credId, AddPerson.key) flatMap {
          case Some(addPerson) => {
            val name = s"${addPerson.firstName} ${addPerson.middleName getOrElse ""} ${addPerson.lastName}"
            for{
              renewalComplete <- DeclarationHelper.renewalComplete(renewalService, request.credId)
              status <- statusService.getStatus(request.amlsRefNumber, request.accountTypeId, request.credId)
            } yield status match {
              case ReadyForRenewal(_) if renewalComplete =>
                Ok(views.html.declaration.declare("declaration.declaration.amendment.title", "submit.renewal.application", name, false))
              case ReadyForRenewal(_) | SubmissionReadyForReview | SubmissionDecisionApproved | RenewalSubmitted(_) =>
                Ok(views.html.declaration.declare("declaration.declaration.amendment.title", "submit.amendment.application", name, true))
              case _ =>
                Ok(views.html.declaration.declare("declaration.declaration.amendment.title", "submit.registration", name, false))
            }
          }
          case _ => redirectToAddPersonPage(request.amlsRefNumber, request.accountTypeId, request.credId)
        }
        case false => Future.successful(Redirect(controllers.routes.RegistrationProgressController.get().url))
      }
    }
  }

  def getWithAmendment = declarationView("declaration.declaration.amendment.title", "submit.amendment.application", true)

  private def declarationView(title: String, subtitle: String, isAmendment: Boolean) = authAction.async {
    implicit request =>
      DeclarationHelper.sectionsComplete(request.credId, sectionsProvider) flatMap {
        case true =>   dataCacheConnector.fetch[AddPerson](request.credId, AddPerson.key) flatMap {
          case Some(addPerson) =>
            val name = s"${addPerson.firstName} ${addPerson.middleName getOrElse ""} ${addPerson.lastName}"
            Future.successful(Ok(views.html.declaration.declare(title, subtitle, name, isAmendment)))
          case _ => redirectToAddPersonPage(request.amlsRefNumber, request.accountTypeId, request.credId)
        }
        case false => Future.successful(Redirect(controllers.routes.RegistrationProgressController.get().url))
      }
  }

  private def redirectToAddPersonPage(amlsRegistrationNo: Option[String], accountTypeId: (String, String), cacheId: String)(implicit hc: HeaderCarrier): Future[Result] =
    statusService.getStatus(amlsRegistrationNo, accountTypeId, cacheId) map {
      case SubmissionReadyForReview => Redirect(routes.AddPersonController.getWithAmendment())
      case _ => Redirect(routes.AddPersonController.get())
    }

}