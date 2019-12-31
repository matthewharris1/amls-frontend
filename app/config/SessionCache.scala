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

package config

import com.google.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

class AmlsSessionCache @Inject()(val configuration: Configuration,
                                 val runMode: RunMode,
                                 val httpClient: HttpClient) extends ServicesConfig(configuration, runMode) with SessionCache {

  override def http = httpClient
  override def defaultSource = getConfString("amls-frontend.cache", "amls-frontend")
  override def baseUri = baseUrl("cachable.session-cache")
  override def domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}

class BusinessCustomerSessionCache @Inject()(val configuration: Configuration,
                                             val runMode: RunMode,
                                             val httpClient: HttpClient) extends ServicesConfig(configuration, runMode) with SessionCache {
  override def http = httpClient
  override def defaultSource: String = getConfString("cachable.session-cache.review-details.cache","business-customer-frontend")
  override def baseUri = baseUrl("cachable.session-cache")
  override def domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}


