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

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

@Singleton
class ApplicationConfig @Inject()(configuration: Configuration, runMode: RunMode, servicesConfig: ServicesConfig) {

  def baseUrl(serviceName: String) = {
    val protocol = servicesConfig.getConfString(s"microservice.services.protocol", "http")
    val host = servicesConfig.getString(s"microservice.services.$serviceName.host")
    val port = servicesConfig.getString(s"microservice.services.$serviceName.port")
    s"$protocol://$host:$port"
  }

  private def getConfigString(key: String) = servicesConfig.getConfString(key, throw new Exception(s"Could not find config '$key'"))
  private def getConfigInt(key: String) = servicesConfig.getConfInt(key, throw new Exception(s"Could not find config '$key'"))

  val contactFormServiceIdentifier = "AMLS"

  lazy val contactHost = baseUrl("contact-frontend")
  lazy val authHost = baseUrl("auth")
  lazy val assetsPrefix = getConfigString(s"assets.url") + getConfigString(s"assets.version")

  lazy val analyticsToken = Some(getConfigString(s"analytics.token"))
  lazy val analyticsHost = getConfigString(s"analytics.host")

  lazy val betaFeedbackUrl = (if (runMode.env == "Prod") "" else contactHost) + getConfigString("contact-frontend.beta-feedback-url.authenticated")
  lazy val betaFeedbackUnauthenticatedUrl = (if (runMode.env == "Prod") "" else contactHost) + getConfigString("contact-frontend.beta-feedback-url.unauthenticated")

  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val loginUrl = getConfigString("login.url")
  def logoutUrl = getConfigString("logout.url")
  lazy val loginContinue = getConfigString("login.continue")

  lazy val paymentsUrl:String = getConfigString("paymentsUrl")

  lazy val timeout = servicesConfig.getInt("timeout.seconds")
  lazy val timeoutCountdown = servicesConfig.getInt("timeout.countdown")

  lazy val ampWhatYouNeedUrl = s"${servicesConfig.getConfString("amls-art-market-participant-frontend.url", "")}/what-you-need"
  lazy val ampSummeryUrl     = s"${servicesConfig.getConfString("amls-art-market-participant-frontend.url", "")}/check-your-answers"

  def businessCustomerUrl = getConfigString("business-customer.url")
  
  lazy val mongoCacheUpdateUrl = baseUrl("amls-stub") + getConfigString("amls-stub.get-file-url")

  // The following values are used by the Fee Guidance Controller currently toggled off with feature-toggle.show-fees
  lazy val regFee = getConfigInt("amounts.registration")
  lazy val premisesFee = getConfigInt("amounts.premises")
  lazy val peopleFeeRate = getConfigInt("amounts.people")
  lazy val approvalCheckPeopleFeeRate = getConfigInt("amounts.approval-check-rate")

  def amlsUrl = baseUrl("amls")

  def subscriptionUrl = s"$amlsUrl/amls/subscription"

  def enrolmentStoreToggle = servicesConfig.getConfBool("feature-toggle.enrolment-store", false)

  def fxEnabledToggle = servicesConfig.getConfBool("feature-toggle.fx-enabled", false)

  lazy val authUrl = baseUrl("auth")

  def enrolmentStoreUrl = baseUrl("tax-enrolments")

  def enrolmentStubsEnabled: Boolean = servicesConfig.getConfBool("enrolment-stubs.enabled", false)

  def enrolmentStubsUrl = baseUrl("enrolment-stubs")

  def feePaymentUrl = s"$amlsUrl/amls/payment"

  def notificationsUrl = baseUrl("amls-notification")

  def allNotificationsUrl = s"$notificationsUrl/amls-notification"

  def ggUrl = baseUrl("government-gateway")

  def enrolUrl = s"$ggUrl/enrol"

  lazy val ggAuthUrl = baseUrl("government-gateway-authentication")

  val mongoEncryptionEnabled = configuration.getOptional[Boolean]("appCache.mongo.encryptionEnabled").getOrElse(true)
  val mongoAppCacheEnabled = configuration.getOptional[Boolean]("appCache.mongo.enabled").getOrElse(false)
  val cacheExpiryInSeconds = configuration.getOptional[Int]("appCache.expiryInSeconds").getOrElse(60)

  def refreshProfileToggle: Boolean = configuration.getOptional[Boolean]("feature-toggle.refresh-profile").getOrElse(false)

  def frontendBaseUrl = {
    val secure = servicesConfig.getConfBool("amls-frontend.public.secure", false)
    val scheme = if (secure) "https" else "http"
    val host = servicesConfig.getConfString("amls-frontend.public.host", "")

    s"$scheme://$host"
  }

  val testOnlyStubsUrl = baseUrl("test-only") + getConfigString("test-only.get-base-url")

  lazy val payBaseUrl = s"${baseUrl("pay-api")}/pay-api"

  lazy val businessMatchingUrl = s"${baseUrl("business-customer")}/business-customer"
}