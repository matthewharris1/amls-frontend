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

package controllers.changeofficer

import javax.inject.Inject

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import controllers.BaseController
import controllers.changeofficer.Helpers._
import forms.{InvalidForm, Form2, ValidForm, EmptyForm}
import models.businessmatching.BusinessMatching
import models.changeofficer.RoleInBusiness
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class RoleInBusinessController @Inject()
(val authConnector: AuthConnector, implicit val dataCacheConnector: DataCacheConnector) extends BaseController {
  def get = Authorised.async {
    implicit authContext => implicit request =>

      val result = getBusinessNameAndName map { t =>
        Ok(views.html.changeofficer.role_in_business(EmptyForm, t._1, t._2))
      }

      result getOrElse InternalServerError("Unable to get nominated officer")
  }

  def post() = Authorised.async {
    implicit authContext => implicit request =>
      import jto.validation.forms.Rules._

      Form2[RoleInBusiness](request.body) match {
        case ValidForm(_, data) =>
          Future.successful(Redirect(controllers.changeofficer.routes.NewOfficerController.get()))
        case f: InvalidForm => {
          val result = getBusinessNameAndName map { t =>
            BadRequest(views.html.changeofficer.role_in_business(f, t._1, t._2))
          }

          result getOrElse InternalServerError("Unable to get nominated officer")
        }
      }
  }

  private def getBusinessNameAndName(implicit authContext: AuthContext, headerCarrier: HeaderCarrier) = {
    for {
      businessMatching <- OptionT(dataCacheConnector.fetch[BusinessMatching](BusinessMatching.key))
      reviewDetails <- OptionT.fromOption[Future](businessMatching.reviewDetails)
      businessType <- OptionT.fromOption[Future](reviewDetails.businessType)
      name <- getNominatedOfficerName()
    } yield (businessType, name)
  }
}
