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

package connectors

import javax.inject.Inject

import audit.ESEnrolEvent
import config.{AppConfig, WSHttp}
import exceptions.DuplicateEnrolmentException
import models.enrolment.{EnrolmentKey, EnrolmentStoreEnrolment}
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.AuthContext
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject()(http: WSHttp, appConfig: AppConfig, auth: AuthConnector, audit: AuditConnector) {

  lazy val baseUrl = s"${appConfig.enrolmentStoreUrl}/enrolment-store-proxy"
  val warn: String => Unit = msg => Logger.warn(s"[EnrolmentStoreConnector] $msg")

  def enrol(enrolKey: EnrolmentKey, enrolment: EnrolmentStoreEnrolment)
           (implicit hc: HeaderCarrier, ac: AuthContext, ec: ExecutionContext): Future[HttpResponse] = {

    auth.userDetails flatMap { details =>
      details.groupIdentifier match {
        case Some(groupId) =>
          val url = s"$baseUrl/enrolment-store/groups/$groupId/enrolments/${enrolKey.key}"

          http.POST[EnrolmentStoreEnrolment, HttpResponse](url, enrolment) map { response =>
            audit.sendEvent(ESEnrolEvent(enrolment, response, enrolKey))
            response
          } recoverWith {
            case e: HttpException if e.responseCode == CONFLICT =>
              warn(s"$CONFLICT received from enrolment-store-proxy: ${e.getMessage}")
              throw DuplicateEnrolmentException(e.getMessage, e)
          }

        case _ => throw new Exception("Group identifier is unavailable")
      }
    }
  }

}
