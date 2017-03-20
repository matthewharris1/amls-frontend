package controllers.renewal

import connectors.DataCacheConnector
import models.renewal.BusinessTurnover
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class BusinessTurnoverControllerSpec extends GenericTestHelper with MockitoSugar with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>
    val request = addToken(authRequest)

    val mockCacheMap = mock[CacheMap]

    val emptyCache = CacheMap("", Map.empty)

    lazy val mockDataCacheConnector = mock[DataCacheConnector]

    val controller = new BusinessTurnoverController(
      dataCacheConnector = mockDataCacheConnector,
      authConnector = self.authConnector
    )
  }


  val emptyCache = CacheMap("", Map.empty)

  "BusinessTurnoverControllerSpec" must {

    "when get is called" must {
      "on get display the  Business Turnover page" in new Fixture {

        when(controller.dataCacheConnector.fetch[BusinessTurnover](any())
          (any(), any(), any())).thenReturn(Future.successful(None))

        val result = controller.get()(request)
        status(result) must be(OK)
        contentAsString(result) must include(Messages("businessactivities.business-turnover.title"))
      }

      "on get display the  Business Turnover page with pre populated data" in new Fixture {

        when(controller.dataCacheConnector.fetch[BusinessTurnover](any())
          (any(), any(), any())).thenReturn(Future.successful(None))

        val result = controller.get()(request)
        status(result) must be(OK)

        val document = Jsoup.parse(contentAsString(result))
        document.select("input[value=01]").hasAttr("checked") must be(true)
      }

      "redirect to Page not found" when {
        "application is in variation mode" in new Fixture {

          val result = controller.get()(request)
          status(result) must be(NOT_FOUND)
        }
      }
    }

    "when post is called" must {

      "on post with valid data" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody(
          "BusinessTurnover" -> "01"
        )

        when(controller.dataCacheConnector.fetch[BusinessTurnover](any())
          (any(), any(), any())).thenReturn(Future.successful(None))

        when(controller.dataCacheConnector.save[BusinessTurnover](any(), any())
          (any(), any(), any())).thenReturn(Future.successful(emptyCache))

        val result = controller.post()(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.businessactivities.routes.AMLSTurnoverController.get().url))
      }

      "on post with valid data in edit mode" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody(
          "BusinessTurnover" -> "01"
        )

        when(controller.dataCacheConnector.fetch[BusinessTurnover](any())
          (any(), any(), any())).thenReturn(Future.successful(None))

        when(controller.dataCacheConnector.save[BusinessTurnover](any(), any())
          (any(), any(), any())).thenReturn(Future.successful(emptyCache))

        val result = controller.post(true)(newRequest)
        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.businessactivities.routes.SummaryController.get().url))
      }

    }
  }
}
