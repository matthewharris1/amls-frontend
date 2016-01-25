package controllers.estateagentbusiness

import connectors.DataCacheConnector
import models.estateagentbusiness.{EstateAgentBusiness, PenalisedUnderEstateAgentsActYes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.Helpers._
import utils.AuthorisedFixture

import scala.concurrent.Future

class PenalisedUnderEstateAgentsActControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new PenalisedUnderEstateAgentsActController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }
  }

  "PenalisedUnderEstateAgentsActController" must {

    "load the blank page when the user visits the first time" in new Fixture {
      when(controller.dataCacheConnector.fetchDataShortLivedCache[EstateAgentBusiness](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include(Messages("estateagentbusiness.penalisedunderestateagentsact.title"))
    }

    "load the page with data when the user revisits at a later time" in new Fixture {
      when(controller.dataCacheConnector.fetchDataShortLivedCache[EstateAgentBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(EstateAgentBusiness(None, None,
        Some(PenalisedUnderEstateAgentsActYes("penalisedunderestateagentsactdetails data"))))))

      val result = controller.get()(request)
      status(result) must be(OK)

      val document: Document = Jsoup.parse(contentAsString(result))
      document.select("input[name=penalisedunderestateagentsact]").`val` must be("true")
    }


    "on post must capture the details provided by the user for penalised before" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "penalisedunderestateagentsact" -> "true",
        "penalisedunderestateagentsactdetails" -> "Do not remember why penalised before"
      )

      when(controller.dataCacheConnector.fetchDataShortLivedCache[EstateAgentBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      when(controller.dataCacheConnector.saveDataShortLivedCache[EstateAgentBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.estateagentbusiness.routes.PenalisedUnderEstateAgentsActController.get().url))
    }


    "on post with missing data must remain on the same page and also retain the data supplied" in new Fixture {

      val requestWithIncompleteData = request.withFormUrlEncodedBody(
        "penalisedunderestateagentsactdetails" -> "Do not remember why penalised before"
      )

      val result = controller.post()(requestWithIncompleteData)
      status(result) must be(BAD_REQUEST)
      val document: Document = Jsoup.parse(contentAsString(result))
      document.select("input[name=penalisedunderestateagentsactdetails]").`val` must be("Do not remember why penalised before")
    }
  }

}