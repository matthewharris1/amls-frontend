/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import cats.data.OptionT
import cats.implicits._
import config.AMLSAuthConnector
import connectors.DataCacheConnector
import models.businessmatching.BusinessMatching
import models.registrationprogress.{Completed, NotStarted, Section, Started}
import models.renewal.Renewal
import models.status._
import play.api.mvc.{AnyContent, Request}
import services.{AuthEnrolmentsService, ProgressService, StatusService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.registrationamendment.registration_amendment
import views.html.registrationprogress.registration_progress

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RegistrationProgressController extends BaseController {

  protected[controllers] def progressService: ProgressService

  protected[controllers] def dataCache: DataCacheConnector

  protected[controllers] def enrolmentsService: AuthEnrolmentsService

  protected[controllers] def statusService: StatusService

  def get() = Authorised.async {
    implicit authContext => implicit request =>
        isRenewalFlow flatMap {
          case true => Future.successful(Redirect(controllers.renewal.routes.RenewalProgressController.get()))
          case _ => {
            (for {
              status <- OptionT.liftF(statusService.getStatus)
              cacheMap <- OptionT(dataCache.fetchAll)
              completePreApp <- OptionT(preApplicationComplete(cacheMap, status))
              businessMatching <- OptionT.fromOption[Future](cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
            } yield {
              businessMatching.reviewDetails map { reviewDetails =>

                val sections = progressService.sections(cacheMap)
                val sectionsToDisplay = sections.filter(s => s.name != BusinessMatching.messageKey)
                val preSubmission = Set(NotCompleted, SubmissionReady).contains(status)

                val activities = businessMatching.activities.fold(Seq.empty[String])(_.businessActivities.map(_.getMessage).toSeq)

                completePreApp match {
                    case true => Ok(registration_amendment(sectionsToDisplay, amendmentDeclarationAvailable(sections), reviewDetails.businessAddress, activities, preSubmission))
                    case _ => Ok(registration_progress(sectionsToDisplay, declarationAvailable(sections), reviewDetails.businessAddress, activities, preSubmission))
                }
              } getOrElse InternalServerError("Unable to retrieve the business details")
            }) getOrElse Redirect(controllers.routes.LandingController.get())
          }
        }
  }

  private def isRenewalFlow()(implicit hc: HeaderCarrier,
                              authContext: AuthContext,
                              request: Request[AnyContent]): Future[Boolean] = {
    statusService.getStatus flatMap {
      case ReadyForRenewal(_) | RenewalSubmitted(_) =>
        dataCache.fetch[Renewal](Renewal.key) map {
          case Some(_) => true
          case None => false
        }

      case _ => Future.successful(false)
    }
  }

  private def declarationAvailable(seq: Seq[Section]): Boolean =
    seq forall {
      _.status == Completed
    }

  private def amendmentDeclarationAvailable(sections: Seq[Section]) = {

    sections.foldLeft((true, false)) { (acc, s) =>
      (acc._1 && s.status == Completed,
        acc._2 || s.hasChanged)
    } match {
      case (true, true) => true
      case _ => false
    }
  }

  private def preApplicationComplete(cache: CacheMap, status: SubmissionStatus)(implicit hc: HeaderCarrier, ac: AuthContext): Future[Option[Boolean]] = {

    val preAppStatus: SubmissionStatus => Boolean = s => Set(NotCompleted, SubmissionReady).contains(s)

    (for {
      bm <- cache.getEntry[BusinessMatching](BusinessMatching.key)
    } yield (preAppStatus(status), bm.isComplete) match {
      case (_, true) | (false, _) =>
        enrolmentsService.amlsRegistrationNumber map {
          case Some(_) => status match {
            case  NotCompleted | SubmissionReady => Some(false)
            case _ => Some(true)
          }
          case None => Some(false)
        }
      case _ => Future.successful(None)
    }).getOrElse(Future.successful(None))
  }

  def post() = Authorised.async {
    implicit authContext =>
      implicit request =>
        progressService.getSubmitRedirect map {
          case Some(url) => Redirect(url)
          case _ => InternalServerError("Could not get data for redirect")
        }
  }
}

object RegistrationProgressController extends RegistrationProgressController {
  // $COVERAGE-OFF$
  override protected[controllers] val authConnector: AuthConnector = AMLSAuthConnector
  override protected[controllers] val progressService = ProgressService
  override protected[controllers] val dataCache = DataCacheConnector
  override protected[controllers] val enrolmentsService = AuthEnrolmentsService
  override protected[controllers] val statusService = StatusService
}
