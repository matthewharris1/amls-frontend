package controllers.estateagentbusiness

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import models.estateagentbusiness.EstateAgentBusiness
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import  utils.GenericTestHelper
import play.api.test.Helpers._
import utils.AuthorisedFixture

import scala.concurrent.Future

class SummaryControllerSpec extends GenericTestHelper with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new SummaryController {
      override val dataCache = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }
  }

  "Get" must {

    "use correct services" in new Fixture {
      SummaryController.authConnector must be(AMLSAuthConnector)
      SummaryController.dataCache must be(DataCacheConnector)
    }

    "load the summary page when section data is available" in new Fixture {

      val model = EstateAgentBusiness(None, None)

      when(controller.dataCache.fetch[EstateAgentBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(model)))

      val result = controller.get()(request)
      status(result) must be(OK)
    }

    "redirect to the main summary page when section data is unavailable" in new Fixture {

      when(controller.dataCache.fetch[EstateAgentBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      val result = controller.get()(request)
      status(result) must be(SEE_OTHER)
    }
  }
}
