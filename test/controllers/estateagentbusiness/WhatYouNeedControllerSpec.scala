package controllers.estateagentbusiness

import config.AMLSAuthConnector
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import utils.AuthorisedFixture
import play.api.i18n.Messages
import play.api.test.Helpers._

class WhatYouNeedControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new WhatYouNeedController {
      override val authConnector = self.authConnector
    }
  }
  "WhatYouNeedController" must {

      "use correct services" in new Fixture {
        WhatYouNeedController.authConnector must be(AMLSAuthConnector)
      }

    "get" must {

      "load the page" in new Fixture {
        val result = controller.get(request)
        status(result) must be(OK)
        contentAsString(result) must include(Messages("estateagentbusiness.whatyouneed.title"))
      }
    }
  }
}
