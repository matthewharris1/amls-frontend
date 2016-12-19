package connectors

import models.Country
import models.businesscustomer.{Address, ReviewDetails}
import models.businessmatching.BusinessType.LimitedCompany
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec

class BusinessMatchingConnectorSpec extends PlaySpec with ScalaFutures {

  val expectedDetails = ReviewDetails("Test Business", Some(LimitedCompany), Address("1 Test Street", "Test Town", None, None, None, Country("UK", "UK")), "some id")

  object TestBusinessMatchingConnector extends BusinessMatchingConnector {

  }

  "The business matching connector" should {


    "get the review details" in {

      whenReady(TestBusinessMatchingConnector.getReviewDetails) { result =>

      }

    }

  }

}
