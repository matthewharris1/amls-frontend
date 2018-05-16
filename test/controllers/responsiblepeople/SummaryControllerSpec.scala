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

package controllers.responsiblepeople

import config.AMLSAuthConnector
import generators.ResponsiblePersonGenerator
import connectors.{AmlsConnector, DataCacheConnector}
import models.Country
import models.businesscustomer.{Address, ReviewDetails}
import models.businessmatching.{BusinessMatching, BusinessType}
import models.responsiblepeople.ResponsiblePerson.{flowChangeOfficer, flowFromDeclaration}
import models.responsiblepeople._
import models.status.{SubmissionDecisionApproved, SubmissionReady, SubmissionReadyForReview}
import org.joda.time.LocalDate
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import services.StatusService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{AuthorisedFixture, DependencyMocks, AmlsSpec}

import scala.concurrent.Future
import uk.gov.hmrc.http.cache.client.CacheMap

class SummaryControllerSpec extends AmlsSpec with MockitoSugar with ResponsiblePersonGenerator {

  trait Fixture extends AuthorisedFixture with DependencyMocks { self =>
    val request = addToken(authRequest)

    lazy val defaultBuilder = new GuiceApplicationBuilder()
      .configure("microservice.services.feature-toggle.show-fees" -> true)
      .disable[com.kenshoo.play.metrics.PlayModule]
      .overrides(bind[AuthConnector].to(self.authConnector))
      .overrides(bind[AmlsConnector].to(mock[AmlsConnector]))
      .overrides(bind[DataCacheConnector].to(mockCacheConnector))
      .overrides(bind[StatusService].to(mockStatusService))

    val builder = defaultBuilder
    lazy val app = builder.build()
    lazy val controller = app.injector.instanceOf[SummaryController]

    val model = responsiblePersonGen.sample.get

    val reviewDetails = ReviewDetails("BusinessName", Some(BusinessType.LimitedCompany),
      Address("line1", "line2", Some("line3"), Some("line4"), Some("AA1 1AA"), Country("United Kingdom", "GB")), "XE0001234567890")

    mockCacheFetch[Seq[ResponsiblePerson]](Some(Seq(model)), Some(ResponsiblePerson.key))

    mockCacheFetch[BusinessMatching](Some(BusinessMatching(Some(reviewDetails), None)), Some(BusinessMatching.key))

  }

  "Get" must {

    "use correct services" in new Fixture {
      controller.authConnector must be(authConnector)
      controller.dataCacheConnector must be(mockCacheConnector)
    }

    "load the summary page when section data is available" in new Fixture {
      val result = controller.get()(request)
      status(result) must be(OK)
    }

    "redirect to the main amls summary page when section data is unavailable" in new Fixture {

      mockCacheFetch[Seq[ResponsiblePerson]](None, Some(ResponsiblePerson.key))

      val result = controller.get()(request)
      redirectLocation(result) must be(Some(controllers.routes.RegistrationProgressController.get().url))
      status(result) must be(SEE_OTHER)
    }

    "show extra content if any of the responsible people are non UK resident" in new Fixture {

      val personName = Some(PersonName("firstname", None, "lastname"))

      mockCacheFetch[Seq[ResponsiblePerson]](Some(Seq(ResponsiblePerson(
        personName,
        personResidenceType = Some(PersonResidenceType(
          NonUKResidence,
          Some(Country("United Kingdom", "GB")),
          Some(Country("France", "FR")))
        )
      ))), Some(ResponsiblePerson.key))

      val result = controller.get()(request)
      status(result) must be(OK)

      contentAsString(result) must include(Messages("responsiblepeople.check_your_answers.hasNonUKresident.1"))
      contentAsString(result) must include(Messages("responsiblepeople.check_your_answers.hasNonUKresident.2"))
    }
  }

  "Post" must {

    "redirect to change officer who is the new nominated officer page" when {
      "flow flag is set to 'changeofficer'" in new Fixture {
        val result = controller.post(Some(flowChangeOfficer))(request)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.changeofficer.routes.NewOfficerController.get().url))
      }
    }

    "redirect to 'registration progress page'" when {
      "'flow flag is not defined" which {
        "will update hasAccepted flag" in new Fixture {

          mockCacheSave[Seq[ResponsiblePerson]]

          val result = controller.post()(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.routes.RegistrationProgressController.get().url))

          verify(controller.dataCacheConnector).save[Seq[ResponsiblePerson]](any(), eqTo(Seq(model.copy(hasAccepted = true))))(any(),any(),any())
        }

        "will strip out empty responsible people models" in new Fixture {
          when {
            controller.dataCacheConnector.save(any(),any())(any(),any(),any())
          } thenReturn Future.successful(CacheMap("", Map.empty))

          val models = Seq(
            responsiblePersonGen.sample.get,
            ResponsiblePerson()
          )

          when {
            controller.dataCacheConnector.fetch[Seq[ResponsiblePerson]](any())(any(), any(), any())
          } thenReturn Future.successful(Some(models))

          val result = controller.post()(request)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.routes.RegistrationProgressController.get().url))

          verify(controller.dataCacheConnector)
            .save[Seq[ResponsiblePerson]](any(), eqTo(Seq(models.head.copy(hasAccepted = true))))(any(),any(),any())
        }
      }
    }

    "redirect to 'Who is the business’s nominated officer?'" when {
      s"'flow flag set to Some($flowFromDeclaration) and status is pending'" in new Fixture {
        val positions = Positions(Set(BeneficialOwner, InternalAccountant), Some(new LocalDate()))
        val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions))
        val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None, None, None, None, None, None, None, Some(positions))
        val responsiblePeople = Seq(rp1, rp2)

        mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
        mockApplicationStatus(SubmissionReady)
        mockCacheSave[Seq[ResponsiblePerson]]

        val result = controller.post(Some(flowFromDeclaration))(request)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.WhoIsTheBusinessNominatedOfficerController.get.url))
      }
      s"'flow flag set to Some($flowFromDeclaration) and status is SubmissionDecisionApproved'" in new Fixture {
        val positions = Positions(Set(BeneficialOwner, InternalAccountant), Some(new LocalDate()))
        val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None,None,None, None, None, None, Some(positions))
        val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None, None, None, None, None, None, None, Some(positions))
        val responsiblePeople = Seq(rp1, rp2)

        mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
        mockApplicationStatus(SubmissionDecisionApproved)
        mockCacheSave[Seq[ResponsiblePerson]]

        val result = controller.post(Some(flowFromDeclaration))(request)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.WhoIsTheBusinessNominatedOfficerController.getWithAmendment().url))
      }
    }

    "redirect to 'Fee Guidance'" when {
      s"'flow flag set to Some($flowFromDeclaration) and status is pre amendment'" in new Fixture {
        val positions = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), Some(new LocalDate()))
        val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions))
        val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None, None, None, None, None, None, None, Some(positions))
        val responsiblePeople = Seq(rp1, rp2)

        mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
        mockApplicationStatus(SubmissionReady)
        mockCacheSave[Seq[ResponsiblePerson]]

        val result = controller.post(Some(flowFromDeclaration))(request)

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.FeeGuidanceController.get().url))
      }
    }

    "redirect to 'Who is registering this business?'" when {
      s"'flow flag set to Some($flowFromDeclaration) and status is SubmissionDecisionApproved'" in new Fixture {
        val positions = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), Some(new LocalDate()))
        val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions))
        val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None, None, None, None, None, None, None, Some(positions))
        val responsiblePeople = Seq(rp1, rp2)

        mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
        mockApplicationStatus(SubmissionDecisionApproved)
        mockCacheSave[Seq[ResponsiblePerson]]

        val result = controller.post(Some(flowFromDeclaration))(request)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.WhoIsRegisteringController.get().url))
      }

      s"'flow flag set to Some($flowFromDeclaration) and status is amendment'" in new Fixture {
        val positions = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), Some(new LocalDate()))
        val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions))
        val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None, None, None,None, None, None, None, Some(positions))
        val responsiblePeople = Seq(rp1, rp2)

        mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
        mockApplicationStatus(SubmissionReadyForReview)
        mockCacheSave[Seq[ResponsiblePerson]]

        val result = controller.post(Some(flowFromDeclaration))(request)

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.WhoIsRegisteringController.get().url))
      }

      s"'flow flag set to Some($flowFromDeclaration) and status is pre amendment'" when {
        "show-fees toggle is off" in new Fixture {

          override val builder = defaultBuilder.configure("microservice.services.feature-toggle.show-fees" -> false)

          val positions = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), Some(new LocalDate()))
          val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions))
          val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None,None, None, None, None, None, None, Some(positions))
          val responsiblePeople = Seq(rp1, rp2)

          mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
          mockApplicationStatus(SubmissionReady)
          mockCacheSave[Seq[ResponsiblePerson]]

          val result = controller.post(Some(flowFromDeclaration))(request)

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(controllers.declaration.routes.WhoIsRegisteringController.get().url))
        }
      }
    }

    "redirect to Register Partners Controller" when {
      "there is less then 2 partners selected" in new Fixture {

        val reviewDetails2 = ReviewDetails("BusinessName", Some(BusinessType.Partnership),
          Address("line1", "line2", Some("line3"), Some("line4"), Some("AA1 1AA"), Country("United Kingdom", "GB")), "XE0001234567890")

        val positions1 = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer, Partner), Some(new LocalDate()))
        val positions2 = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), Some(new LocalDate()))
        val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions1))
        val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None,None, None, None, None, None, None, Some(positions2))
        val responsiblePeople = Seq(rp1, rp2)

        mockCacheFetch[BusinessMatching](Some(BusinessMatching(Some(reviewDetails2), None)), Some(BusinessMatching.key))
        mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
        mockApplicationStatus(SubmissionReady)
        mockCacheSave[Seq[ResponsiblePerson]]

        val result = controller.post(Some(flowFromDeclaration))(request)

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.RegisterPartnersController.get().url))

      }
    }

    "return INTERNAL_SERVER_ERROR if reviewdetails cannot be retrieved" in new Fixture {

      val positions1 = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer, Partner), Some(new LocalDate()))
      val positions2 = Positions(Set(BeneficialOwner, InternalAccountant, NominatedOfficer), Some(new LocalDate()))
      val rp1 = ResponsiblePerson(Some(PersonName("first", Some("middle"), "last")), None, None, None, None, None, None, None, None, None, Some(positions1))
      val rp2 = ResponsiblePerson(Some(PersonName("first2", None, "middle2")), None, None, None,None, None, None, None, None, None, Some(positions2))
      val responsiblePeople = Seq(rp1, rp2)

      mockCacheFetch[BusinessMatching](Some(BusinessMatching(None, None)), Some(BusinessMatching.key))
      mockCacheFetch[Seq[ResponsiblePerson]](Some(responsiblePeople), Some(ResponsiblePerson.key))
      mockApplicationStatus(SubmissionReady)
      mockCacheSave[Seq[ResponsiblePerson]]

      val result = controller.post(Some(flowFromDeclaration))(request)

      status(result) must be(INTERNAL_SERVER_ERROR)

    }


  }
}
