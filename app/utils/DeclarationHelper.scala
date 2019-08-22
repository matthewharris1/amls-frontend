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

package utils

import controllers.declaration
import models.responsiblepeople.{Partner, ResponsiblePerson}
import models.status._
import services.StatusService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object DeclarationHelper {

  def currentPartnersNames(responsiblePeople: Seq[ResponsiblePerson]): Seq[String] = {
    nonDeletedResponsiblePeopleWithPositions(responsiblePeople).collect {
      case p if p.positions.get.positions.contains(Partner) => p.personName.get.fullName
    }
  }

  def numberOfPartners(responsiblePeople: Seq[ResponsiblePerson]): Int = {

    nonDeletedResponsiblePeopleWithPositions(responsiblePeople).collect({
      case p if p.positions.get.positions.contains(Partner) => p
    }).size
  }

  def nonPartners(responsiblePeople: Seq[ResponsiblePerson]): Seq[ResponsiblePerson] = {
    nonDeletedResponsiblePeopleWithPositions(responsiblePeople).collect({
      case p if !p.positions.get.positions.contains(Partner) => p
    })
  }

  private def nonDeletedResponsiblePeopleWithPositions(responsiblePeople: Seq[ResponsiblePerson]): Seq[ResponsiblePerson] = {
    responsiblePeople.collect {
      case p if p.positions.isDefined & p.status.isEmpty => p
    }
  }

  def routeDependingOnNominatedOfficer(hasNominatedOfficer: Boolean, status: SubmissionStatus) = {
    hasNominatedOfficer match {
      case true => declaration.routes.WhoIsRegisteringController.get()
      case false => routeWithoutNominatedOfficer(status)
    }
  }

  private def routeWithoutNominatedOfficer(status: SubmissionStatus) = {
    status match {
      case SubmissionReady | NotCompleted | SubmissionReadyForReview | ReadyForRenewal(_) =>
        declaration.routes.WhoIsTheBusinessNominatedOfficerController.get()
      case _ => declaration.routes.WhoIsTheBusinessNominatedOfficerController.getWithAmendment()
    }
  }
@deprecated("To be removed when auth upgrade is in place")
  def statusSubtitle()(implicit statusService: StatusService, hc: HeaderCarrier, auth: AuthContext): Future[String] = {
    statusService.getStatus map {
      case SubmissionReady => "submit.registration"
      case SubmissionReadyForReview | SubmissionDecisionApproved => "submit.amendment.application"
      case ReadyForRenewal(_) | RenewalSubmitted(_) => "submit.renewal.application"
      case _ => throw new Exception("Incorrect status - Page not permitted for this status")
    }
  }

  def statusSubtitle(amlsRegistrationNo: Option[String], accountTypeId: (String, String), cacheId: String)(implicit statusService: StatusService, hc: HeaderCarrier): Future[String] = {
    statusService.getStatus(amlsRegistrationNo, accountTypeId, cacheId) map {
      case SubmissionReady => "submit.registration"
      case SubmissionReadyForReview | SubmissionDecisionApproved => "submit.amendment.application"
      case ReadyForRenewal(_) | RenewalSubmitted(_) => "submit.renewal.application"
      case _ => throw new Exception("Incorrect status - Page not permitted for this status")
    }
  }
}
