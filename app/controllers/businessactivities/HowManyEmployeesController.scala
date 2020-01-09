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

package controllers.businessactivities

import com.google.inject.Inject
import connectors.DataCacheConnector
import controllers.DefaultBaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.businessactivities.{BusinessActivities, EmployeeCount, HowManyEmployees}
import utils.AuthAction
import views.html.businessactivities._

import scala.concurrent.Future

class HowManyEmployeesController @Inject() (val dataCacheConnector: DataCacheConnector,
                                            val authAction: AuthAction
                                           ) extends DefaultBaseController {

  def updateData(howManyEmployees: Option[HowManyEmployees], data: EmployeeCount): HowManyEmployees = {
    howManyEmployees.fold[HowManyEmployees](HowManyEmployees(employeeCount = Some(data.employeeCount)))(x =>
      x.copy(employeeCount = Some(data.employeeCount)))
  }

  def get(edit: Boolean = false) = authAction.async {
      implicit request => {
        dataCacheConnector.fetch[BusinessActivities](request.credId, BusinessActivities.key) map {
          response =>
            val form: Form2[HowManyEmployees] = (for {
              businessActivities <- response
              employees <- businessActivities.howManyEmployees
            } yield Form2[HowManyEmployees](employees)).getOrElse(EmptyForm)
            Ok(business_employees(form, edit))
        }
      }
  }

  def post(edit: Boolean = false) = authAction.async {
    implicit request => {
      Form2[EmployeeCount](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(business_employees(f, edit)))
        case ValidForm(_, data) =>
          for {
            businessActivities <- dataCacheConnector.fetch[BusinessActivities](request.credId, BusinessActivities.key)
            _ <- dataCacheConnector.save[BusinessActivities](request.credId, BusinessActivities.key,
              businessActivities.howManyEmployees(updateData(businessActivities.howManyEmployees, data)))
          } yield edit match {
            case true => Redirect(routes.SummaryController.get())
            case false => Redirect(routes.TransactionRecordController.get())
          }
      }
    }
  }
}
