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

package controllers.businessmatching.updateservice.remove

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import generators.ResponsiblePersonGenerator
import generators.tradingpremises.TradingPremisesGenerator
import models.DateOfChange
import models.businessactivities.BusinessActivities
import models.businessmatching.{BusinessActivities => BMBusinessActivities, _}
import models.responsiblepeople.ResponsiblePeople
import models.supervision.Supervision
import models.tradingpremises.TradingPremises
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{MustMatchers, PrivateMethodTester}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Result, Results}
import play.api.test.Helpers._
import services.businessmatching.BusinessMatchingService
import services.{StatusService, TradingPremisesService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{AuthorisedFixture, DependencyMocks, AmlsSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpdateServiceDateOfChangeControllerSpec extends AmlsSpec
  with MockitoSugar
  with MustMatchers
  with PrivateMethodTester
  with Results
  with TradingPremisesGenerator
  with ResponsiblePersonGenerator {

  trait Fixture extends AuthorisedFixture with DependencyMocks { self =>

    val request = addToken(authRequest)

    val mockBusinessMatchingService = mock[BusinessMatchingService]
    val mockTradingPremisesService = mock[TradingPremisesService]

    lazy val app = new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .overrides(bind[DataCacheConnector].to(mockCacheConnector))
      .overrides(bind[StatusService].to(mockStatusService))
      .overrides(bind[AuthConnector].to(self.authConnector))
      .overrides(bind[BusinessMatchingService].to(mockBusinessMatchingService))
      .overrides(bind[TradingPremisesService].to(mockTradingPremisesService))
      .build()

    val controller = app.injector.instanceOf[UpdateServiceDateOfChangeController]

    when {
      mockBusinessMatchingService.updateModel(any())(any(),any(),any())
    } thenReturn OptionT.some[Future, CacheMap](mockCacheMap)

    val responsiblePerson = responsiblePersonGen.sample.get.copy(hasAlreadyPassedFitAndProper = None)
    val responsiblePersonChanged = responsiblePerson.copy(hasChanged = true, hasAccepted = true)

    val fitAndProperResponsiblePeople = Seq(
      responsiblePerson.copy(hasAlreadyPassedFitAndProper = Some(true)),
      responsiblePerson.copy(hasAlreadyPassedFitAndProper = Some(false))
    )

    mockCacheFetch[Seq[ResponsiblePeople]](Some(fitAndProperResponsiblePeople), Some(ResponsiblePeople.key))
    mockCacheSave[Seq[ResponsiblePeople]]
    mockCacheFetch[BusinessActivities](Some(BusinessActivities()), Some(BusinessActivities.key))
    mockCacheSave[BusinessActivities]

  }

  "UpdateServiceDateOfChangeControllerSpec" when {

    "get is called" must {
      "display date_of_change view" in new Fixture {

        val result = controller.get("01")(request)

        status(result) must be(OK)
        Jsoup.parse(contentAsString(result)).title() must include(Messages("dateofchange.title"))
      }
    }

    "post is called" must {
      "redirect to UpdateAnyInformationController" when {
        "request is valid" in new Fixture {

          val tradingPremises = Seq(
            tradingPremisesGen.sample.get,
            tradingPremisesGen.sample.get
          )

          mockCacheFetch[Seq[TradingPremises]](Some(tradingPremises), Some(TradingPremises.key))
          mockCacheSave[BusinessMatching]

          when {
            mockBusinessMatchingService.getModel(any(),any(),any())
          } thenReturn OptionT.some[Future, BusinessMatching](BusinessMatching(
            activities = Some(BMBusinessActivities(Set(
              EstateAgentBusinessService,
              HighValueDealing
            )))
          ))

          when {
            mockBusinessMatchingService.clearSection(any())(any(),any())
          } thenReturn Future.successful(mockCacheMap)

          val result = controller.post("03")(request.withFormUrlEncodedBody(
            "dateOfChange.day" -> "13",
            "dateOfChange.month" -> "10",
            "dateOfChange.year" -> "2017"
          ))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.businessmatching.updateservice.routes.UpdateAnyInformationController.get().url))

        }
      }
      "respond with BAD_REQUEST" when {
        "request is invalid" in new Fixture {

          val result = controller.post("")(request)

          status(result) must be(BAD_REQUEST)
        }
      }
    }

    "mapRequestToActivities" must {

      "respond with list of services" in new Fixture {

        val mapRequestToActivities = PrivateMethod[Either[Result, Set[BusinessActivity]]]('mapRequestToActivities)

        val result = controller invokePrivate mapRequestToActivities("03/04")

        result must be(Right(Set(
          EstateAgentBusinessService,
          HighValueDealing
        )))

      }

      "respond with BAD_REQUEST" when {
        "request contains id not linked to business activities" in new Fixture {

          val mapRequestToActivities = PrivateMethod[Either[Result, Set[BusinessActivity]]]('mapRequestToActivities)

          val result = controller invokePrivate mapRequestToActivities("01/123/03")

          result must be(Left(BadRequest))

        }
        "request contains invalid string in sequence" in new Fixture {

          val mapRequestToActivities = PrivateMethod[Either[Result, Set[BusinessActivity]]]('mapRequestToActivities)

          val result = controller invokePrivate mapRequestToActivities("03/abc/04")

          result must be(Left(BadRequest))

        }
        "request contains empty string" in new Fixture {

          val mapRequestToActivities = PrivateMethod[Either[Result, Set[BusinessActivity]]]('mapRequestToActivities)

          val result = controller invokePrivate mapRequestToActivities("")

          result must be(Left(BadRequest))

        }
      }
    }

    "fitAndProperRequired" must {
      "return true" when {
        "tcsp is defined in businessActivities" in new Fixture {

          val fitAndProperRequired = PrivateMethod[Boolean]('fitAndProperRequired)

          val result = controller invokePrivate fitAndProperRequired(BMBusinessActivities(Set(TrustAndCompanyServices), None))

          result must be(true)

        }
        "msb is defined in businessActivities" in new Fixture {

          val fitAndProperRequired = PrivateMethod[Boolean]('fitAndProperRequired)

          val result = controller invokePrivate fitAndProperRequired(BMBusinessActivities(Set(MoneyServiceBusiness), None))

          result must be(true)

        }
      }
      "return false" when {
        "neither msb or tcsp appear businessActivities" in new Fixture {

          val fitAndProperRequired = PrivateMethod[Boolean]('fitAndProperRequired)

          val result = controller invokePrivate fitAndProperRequired(BMBusinessActivities(Set(HighValueDealing), None))

          result must be(false)

        }
      }
    }

  }

  it must {

    "save the DateOfChange to BusinessActivities and update Trading Premises" in new Fixture {

      val tradingPremises = Seq(
        tradingPremisesGen.sample.get,
        tradingPremisesGen.sample.get
      )

      mockCacheFetch[Seq[TradingPremises]](Some(tradingPremises), Some(TradingPremises.key))
      mockCacheSave[BusinessMatching]
      mockCacheSave[Seq[BusinessMatching]]
      mockCacheSave[Supervision]

      when {
        mockBusinessMatchingService.getModel(any(),any(),any())
      } thenReturn OptionT.some[Future, BusinessMatching](BusinessMatching(
        activities = Some(BMBusinessActivities(Set(
          EstateAgentBusinessService,
          HighValueDealing,
          TelephonePaymentService
        )))
      ))

      when {
        mockTradingPremisesService.removeBusinessActivitiesFromTradingPremises(any(),any(),any())
      } thenReturn tradingPremises

      when {
        mockBusinessMatchingService.clearSection(any())(any(),any())
      } thenReturn Future.successful(mockCacheMap)

      val result = controller.post("01/02/03/04/05/06")(request.withFormUrlEncodedBody(
        "dateOfChange.day" -> "13",
        "dateOfChange.month" -> "10",
        "dateOfChange.year" -> "2017"
      ))

      status(result) must be(SEE_OTHER)

      verify(mockBusinessMatchingService).updateModel(
        eqTo(BusinessMatching(
          activities = Some(BMBusinessActivities(
            Set(TelephonePaymentService),
            None,
            Some(Set(
              HighValueDealing,
              AccountancyServices,
              EstateAgentBusinessService,
              BillPaymentServices,
              MoneyServiceBusiness,
              TrustAndCompanyServices
            )),
            Some(DateOfChange(new LocalDate(2017,10,13)))
          )),
          hasChanged = true,
          hasAccepted = true
        )))(any(),any(),any())

      verify(mockCacheConnector).save[Seq[TradingPremises]](
        eqTo(TradingPremises.key),
        eqTo(tradingPremises)
      )(any(),any(),any())

      verify(mockCacheConnector).save[Supervision](
        eqTo(Supervision.key),
        eqTo(None)
      )(any(),any(),any())

      verify(mockBusinessMatchingService).clearSection(eqTo(AccountancyServices))(any(),any())

      verify(mockBusinessMatchingService).clearSection(eqTo(EstateAgentBusinessService))(any(),any())

      verify(mockBusinessMatchingService).clearSection(eqTo(HighValueDealing))(any(),any())

      verify(mockBusinessMatchingService).clearSection(eqTo(MoneyServiceBusiness))(any(),any())

      verify(mockBusinessMatchingService).clearSection(eqTo(TrustAndCompanyServices))(any(),any())
    }

    "remove fitAndProper from rp" when {
      "fitAndProper is not required" in new Fixture {

        val tradingPremises = Seq(
          tradingPremisesGen.sample.get,
          tradingPremisesGen.sample.get
        )

        mockCacheFetch[Seq[TradingPremises]](Some(tradingPremises), Some(TradingPremises.key))
        mockCacheSave[BusinessMatching]
        mockCacheSave[Seq[BusinessMatching]]
        mockCacheSave[Supervision]

        when {
          mockBusinessMatchingService.getModel(any(),any(),any())
        } thenReturn OptionT.some[Future, BusinessMatching](BusinessMatching(
          activities = Some(BMBusinessActivities(Set(
            EstateAgentBusinessService,
            HighValueDealing,
            TrustAndCompanyServices
          )))
        ))

        when {
          mockTradingPremisesService.removeBusinessActivitiesFromTradingPremises(any(),any(),any())
        } thenReturn tradingPremises

        when {
          mockBusinessMatchingService.clearSection(any())(any(),any())
        } thenReturn Future.successful(mockCacheMap)

        val result = controller.post("06")(request.withFormUrlEncodedBody(
          "dateOfChange.day" -> "13",
          "dateOfChange.month" -> "10",
          "dateOfChange.year" -> "2017"
        ))

        status(result) must be(SEE_OTHER)

        verify(mockCacheConnector).save[Seq[ResponsiblePeople]](
          eqTo(ResponsiblePeople.key),
          eqTo(Seq(responsiblePersonChanged, responsiblePersonChanged))
        )(any(),any(),any())

      }
    }

  }

}