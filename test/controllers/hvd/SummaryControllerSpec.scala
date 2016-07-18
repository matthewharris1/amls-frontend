package controllers.hvd

import connectors.DataCacheConnector
import models.hvd.Hvd
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.Helpers._
import utils.AuthorisedFixture

import scala.concurrent.Future

class SummaryControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new SummaryController {
      override val dataCache = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }
  }

  "Get" must {

    "load the summary page when section data is available" in new Fixture {

      val model = Hvd(None)

      when(controller.dataCache.fetch[Hvd](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(model)))

      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include (Messages("summary.checkyouranswers.title"))
    }

    "redirect to the main summary page when section data is unavailable" in new Fixture {

      when(controller.dataCache.fetch[Hvd](any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      val result = controller.get()(request)
      status(result) must be(SEE_OTHER)
    }
  }
}