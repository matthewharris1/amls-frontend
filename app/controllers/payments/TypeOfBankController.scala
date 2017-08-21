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

package controllers.payments

import javax.inject.Inject

import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.payments.TypeOfBank
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class TypeOfBankController @Inject()(
                                    val authConnector: AuthConnector
                                    ) extends BaseController{

  def get() = Authorised.async {
    implicit authContext => implicit request =>
      Future.successful(Ok(views.html.payments.type_of_bank(EmptyForm)))
  }

  def post() = Authorised.async {
    implicit authContext =>
      implicit request =>
        Form2[TypeOfBank](request.body) match {
          case ValidForm(_, data) => Future.successful(Redirect(controllers.payments.routes.BankDetailsController.get(
            data.isUK
          ).url))
          case f: InvalidForm => Future.successful(BadRequest(views.html.payments.type_of_bank(f)))
        }
  }

}