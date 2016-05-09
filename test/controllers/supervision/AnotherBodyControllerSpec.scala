package controllers.supervision

import connectors.DataCacheConnector
import models.supervision.{ProfessionalBodyYes, AnotherBodyYes, Supervision}
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.AuthorisedFixture

import scala.concurrent.Future


class AnotherBodyControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new AnotherBodyController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }
  }

  val emptyCache = CacheMap("", Map.empty)

  "PenalisedByProfessionalController" must {

    "on get display the Penalised By Professional Body page" in new Fixture {
      when(controller.dataCacheConnector.fetch[Supervision](any())
        (any(), any(), any())).thenReturn(Future.successful(None))
      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include(Messages("supervision.penalisedbyprofessional.title"))
    }


  "on get display the Penalised By Professional Body page with pre populated data" in new Fixture {
    val start = new LocalDate(1990, 2, 24) //scalastyle:off magic.number
    val end = new LocalDate(1998, 2, 24)   //scalastyle:off magic.number
    when(controller.dataCacheConnector.fetch[Supervision](any())
      (any(), any(), any())).thenReturn(Future.successful(Some(Supervision(Some(AnotherBodyYes("Name", start, end, "Reason")), Some(ProfessionalBodyYes("details"))))))

    val result = controller.get()(request)
    status(result) must be(OK)
    contentAsString(result) must include ("Reason")

  }

  "on post with valid data" in new Fixture {

    val newRequest = request.withFormUrlEncodedBody(
      "anotherBody" -> "true",
      "supervisorName" -> "Name",
      "startDate.day" -> "24",
      "startDate.month" -> "2",
      "startDate.year" -> "1990",
      "endDate.day" -> "24",
      "endDate.month" -> "2",
      "endDate.year" -> "1998",
      "endingReason" -> "Reason"
    )

    when(controller.dataCacheConnector.fetch[Supervision](any())
      (any(), any(), any())).thenReturn(Future.successful(None))

    when(controller.dataCacheConnector.save[Supervision](any(), any())
      (any(), any(), any())).thenReturn(Future.successful(emptyCache))

    val result = controller.post()(newRequest)
    status(result) must be(SEE_OTHER)
    redirectLocation(result) must be(Some(controllers.supervision.routes.SummaryController.get().url))
  }

  "on post with invalid data" in new Fixture {

    val newRequest = request.withFormUrlEncodedBody(

    )

    val result = controller.post()(newRequest)
    status(result) must be(BAD_REQUEST)

    val document = Jsoup.parse(contentAsString(result))

  }

   "on post with valid data in edit mode" in new Fixture {

     val newRequest = request.withFormUrlEncodedBody(
       "anotherBody" -> "false"
     )

     when(controller.dataCacheConnector.fetch[Supervision](any())
       (any(), any(), any())).thenReturn(Future.successful(None))

     when(controller.dataCacheConnector.save[Supervision](any(), any())
       (any(), any(), any())).thenReturn(Future.successful(emptyCache))

     val result = controller.post(true)(newRequest)
     status(result) must be(SEE_OTHER)
     redirectLocation(result) must be(Some(controllers.supervision.routes.SummaryController.get().url))
   }
  }
}


