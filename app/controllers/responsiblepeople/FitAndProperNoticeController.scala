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

package controllers.responsiblepeople

import config.AppConfig
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{Form2, _}
import javax.inject.{Inject, Singleton}
import models.businessmatching.BusinessMatching
import models.responsiblepeople.ResponsiblePerson
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{ControllerHelper, RepeatingSection}

import scala.concurrent.Future

@Singleton
class FitAndProperNoticeController @Inject()(
                                        val dataCacheConnector: DataCacheConnector,
                                        val authConnector: AuthConnector,
                                        appConfig: AppConfig
                                      ) extends RepeatingSection with BaseController {

  def get(index: Int, edit: Boolean = false, flow: Option[String] = None) = Authorised.async {
    implicit authContext => implicit request =>
        Future(Ok(views.html.responsiblepeople.fit_and_proper_notice(EmptyForm, edit, index, flow, "",
            appConfig.showFeesToggle, appConfig.phase2ChangesToggle)))
  }
}