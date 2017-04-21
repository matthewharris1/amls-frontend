package controllers.businessactivities

import connectors.DataCacheConnector
import models.businessactivities.{BusinessActivities, InvolvedInOtherYes}
import models.businessmatching.{BusinessActivities => BMActivities, _}
import models.status.{NotCompleted, SubmissionDecisionApproved}
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.PrivateMethodTester
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import  utils.GenericTestHelper
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.StatusService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.AuthorisedFixture

import scala.concurrent.Future

class InvolvedInOtherControllerSpec extends GenericTestHelper with MockitoSugar with ScalaFutures with PrivateMethodTester{

  trait Fixture extends AuthorisedFixture {
    self => val request = addToken(authRequest)

     val controller = new InvolvedInOtherController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
       override implicit val statusService: StatusService = mock[StatusService]
     }
  }

  val emptyCache = CacheMap("", Map.empty)

  val mockCacheMap = mock[CacheMap]

  "InvolvedInOtherController" when {

    "get is called" must {
      "display the is your involved in other page with an empty form" in new Fixture {

        val businessMatching = BusinessMatching(
          activities = Some(BMActivities(Set(AccountancyServices, BillPaymentServices, EstateAgentBusinessService,
            HighValueDealing, MoneyServiceBusiness, TrustAndCompanyServices, TelephonePaymentService)))
        )

        when(controller.statusService.getStatus(any(), any(), any()))
          .thenReturn(Future.successful(NotCompleted))

        when(mockCacheMap.getEntry[BusinessActivities](BusinessActivities.key))
          .thenReturn(None)

        when(mockCacheMap.getEntry[BusinessMatching](BusinessMatching.key))
          .thenReturn(Some(businessMatching))

        when(controller.dataCacheConnector.fetchAll(any[HeaderCarrier], any[AuthContext]))
          .thenReturn(Future.successful(Some(mockCacheMap)))

        val result = controller.get()(request)
        status(result) must be(OK)

        val html = contentAsString(result)

        html must include(Messages("businessmatching.registerservices.servicename.lbl.01"))
        html must include(Messages("businessmatching.registerservices.servicename.lbl.02"))
        html must include(Messages("businessmatching.registerservices.servicename.lbl.03"))
        html must include(Messages("businessmatching.registerservices.servicename.lbl.04"))
        html must include(Messages("businessmatching.registerservices.servicename.lbl.05"))
        html must include(Messages("businessmatching.registerservices.servicename.lbl.06"))
        html must include(Messages("businessmatching.registerservices.servicename.lbl.07"))

        val page = Jsoup.parse(html)

        page.select("input[type=radio][name=involvedInOther][value=true]").hasAttr("checked") must be(false)
        page.select("input[type=radio][name=involvedInOther][value=false]").hasAttr("checked") must be(false)
        page.select("textarea[name=details]").`val` must be("")
      }

      "display the is your involved in other page when there is no cache data" in new Fixture {

        val businessMatching = BusinessMatching(
          activities = Some(BMActivities(Set(AccountancyServices, BillPaymentServices, EstateAgentBusinessService,
            HighValueDealing, MoneyServiceBusiness, TrustAndCompanyServices, TelephonePaymentService)))
        )

        when(controller.statusService.getStatus(any(), any(), any()))
          .thenReturn(Future.successful(NotCompleted))

        when(controller.dataCacheConnector.fetchAll(any[HeaderCarrier], any[AuthContext]))
          .thenReturn(Future.successful(None))

        val result = controller.get()(request)
        status(result) must be(OK)

      }

      "display the involved in other page with pre populated data" in new Fixture {

        when(mockCacheMap.getEntry[BusinessActivities](BusinessActivities.key))
          .thenReturn(Some(BusinessActivities(involvedInOther = Some(InvolvedInOtherYes("test")))))

        when(controller.statusService.getStatus(any(), any(), any()))
          .thenReturn(Future.successful(NotCompleted))

        when(mockCacheMap.getEntry[BusinessMatching](BusinessMatching.key))
          .thenReturn(Some(BusinessMatching()))

        when(controller.dataCacheConnector.fetchAll(any(), any()))
          .thenReturn(Future.successful(Some(mockCacheMap)))

        val result = controller.get()(request)
        status(result) must be(OK)

        val page = Jsoup.parse(contentAsString(result))

        page.select("input[type=radio][name=involvedInOther][value=true]").hasAttr("checked") must be(true)
        page.select("input[type=radio][name=involvedInOther][value=false]").hasAttr("checked") must be(false)
        page.select("textarea[name=details]").`val` must be("test")
      }

      "redirect to Page not found" when {
        "allowedToEdit is false (status is not SubmissionReady | NotCompleted | SubmissionReadyForReview)" in new Fixture {

          when(controller.statusService.getStatus(any(), any(), any()))
            .thenReturn(Future.successful(SubmissionDecisionApproved))

          val result = controller.get()(request)
          status(result) must be(NOT_FOUND)
        }
      }
    }

    "post is called" must {
      "respond with SEE_OTHER" when {
        "edit is false" when {
          "involvedInOther is true and there is no existing BusinessActivities data" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "involvedInOther" -> "true",
              "details" -> "test"
            )

            when(controller.dataCacheConnector.fetch[BusinessActivities](any())(any(), any(), any()))
              .thenReturn(Future.successful(None))

            when(controller.dataCacheConnector.save[BusinessActivities](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post()(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.ExpectedBusinessTurnoverController.get().url))
          }
          "involvedInOther is true and there is existing BusinessActivities data" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "involvedInOther" -> "true",
              "details" -> "test"
            )

            when(controller.dataCacheConnector.fetch[BusinessActivities](any())(any(), any(), any()))
              .thenReturn(Future.successful(Some(BusinessActivities())))

            when(controller.dataCacheConnector.save[BusinessActivities](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post()(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.ExpectedBusinessTurnoverController.get().url))
          }

          "involvedInOther is false and there is no existing BusinessActivities data" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "involvedInOther" -> "false"
            )

            when(controller.dataCacheConnector.fetch[BusinessActivities](any())(any(), any(), any()))
              .thenReturn(Future.successful(None))

            when(controller.dataCacheConnector.save[BusinessActivities](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post()(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.ExpectedAMLSTurnoverController.get().url))
          }

          "involvedInOther is false and there is existing BusinessActivities data" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "involvedInOther" -> "false"
            )

            when(controller.dataCacheConnector.fetch[BusinessActivities](any())(any(), any(), any()))
              .thenReturn(Future.successful(Some(BusinessActivities())))

            when(controller.dataCacheConnector.save[BusinessActivities](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post()(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.ExpectedAMLSTurnoverController.get().url))
          }
        }
        "edit is true" when {
          "involvedInOther is true" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "involvedInOther" -> "true",
              "details" -> "test"
            )

            when(controller.dataCacheConnector.fetch[BusinessActivities](any())(any(), any(), any()))
              .thenReturn(Future.successful(None))

            when(controller.dataCacheConnector.save[BusinessActivities](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post(true)(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.ExpectedBusinessTurnoverController.get(true).url))
          }

          "involvedInOther is false" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "involvedInOther" -> "false"
            )

            when(controller.dataCacheConnector.fetch[BusinessActivities](any())(any(), any(), any()))
              .thenReturn(Future.successful(None))

            when(controller.dataCacheConnector.save[BusinessActivities](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post(true)(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(routes.SummaryController.get().url))
          }
        }
      }

      "respond with BAD_REQUEST" when {
        "given invalid data" in new Fixture {

          when(controller.dataCacheConnector.fetch[BusinessMatching](any())(any(), any(), any()))
            .thenReturn(Future.successful(None))

          val newRequest = request.withFormUrlEncodedBody(
            "involvedInOther" -> "test"
          )

          val result = controller.post()(newRequest)
          status(result) must be(BAD_REQUEST)

          contentAsString(result) must include(Messages("error.required.ba.involved.in.other"))
        }

        "on post with required field not filled with business activities" in new Fixture {

          val businessMatching = BusinessMatching(
            activities = Some(BMActivities(Set(AccountancyServices)))
          )

          when(controller.dataCacheConnector.fetch[BusinessMatching](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(businessMatching)))

          val newRequest = request.withFormUrlEncodedBody(
            "involvedInOther" -> "true",
            "details" -> ""
          )

          val result = controller.post()(newRequest)
          status(result) must be(BAD_REQUEST)

          contentAsString(result) must include(Messages("error.required.ba.involved.in.other.text"))
        }
      }


    }
  }
}
