package controllers.msb

import connectors.DataCacheConnector
import models.moneyservicebusiness._
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.AuthorisedFixture
import play.api.test.Helpers._

import scala.concurrent.Future

class IdentifyLinkedTransactionsControllerSpec extends PlaySpec with OneServerPerSuite with MockitoSugar  {

  trait Fixture extends AuthorisedFixture {
    self =>

    val controller = new IdentifyLinkedTransactionsController {
      override val dataCacheConnector: DataCacheConnector = mock[DataCacheConnector]
      override val authConnector: AuthConnector = self.authConnector
    }
  }

  val completedMT = Some(BusinessAppliedForPSRNumberNo)
  val completedCE = Some(CETransactionsInNext12Months("10"))

  val emptyCache = CacheMap("", Map.empty)

  "IdentifyLinkedTransactionsController" must {

    "load the page systems identify linked transactions" in new Fixture {

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include(Messages("msb.linked.txn.title"))
    }

    "load the page systems identify linked transactions with pre populated data" in new Fixture  {

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(MoneyServiceBusiness(
        identifyLinkedTransactions = Some(IdentifyLinkedTransactions(true))))))

      val result = controller.get()(request)
      status(result) must be(OK)
      contentAsString(result) must include(Messages("msb.linked.txn.title"))

    }

    "Show error message when user has not filled the mandatory fields" in new Fixture  {

      val newRequest = request.withFormUrlEncodedBody(
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include (Messages("error.required.msb.linked.txn"))

    }

    "Navigate to next page if they have selected MT as a service" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(
          MsbServices(
            Set(
              TransmittingMoney,
              CurrencyExchange,
              ChequeCashingNotScrapMetal,
              ChequeCashingScrapMetal
            )
          )
        )
      )

      val outgoingModel = incomingModel.copy(
        identifyLinkedTransactions = Some(
          IdentifyLinkedTransactions(true)
        )
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key))
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key), eqTo(outgoingModel))
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.BusinessAppliedForPSRNumberController.get().url))
    }

    "Navigate to next page if they have selected CE as a service" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(
          MsbServices(
            Set(
              CurrencyExchange,
              ChequeCashingNotScrapMetal,
              ChequeCashingScrapMetal
            )
          )
        )
      )

      val outgoingModel = incomingModel.copy(
        identifyLinkedTransactions = Some(
          IdentifyLinkedTransactions(true)
        )
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key))
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key), eqTo(outgoingModel))
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.CETransactionsInNext12MonthsController.get().url))
    }

    "Navigate to next page if they have selected cheque cashing as a service" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(
          MsbServices(
            Set(
              ChequeCashingNotScrapMetal,
              ChequeCashingScrapMetal
            )
          )
        )
      )

      val outgoingModel = incomingModel.copy(
        identifyLinkedTransactions = Some(
          IdentifyLinkedTransactions(true)
        )
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key))
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](eqTo(MoneyServiceBusiness.key), eqTo(outgoingModel))
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.SummaryController.get().url))
    }

    "Navigate to Summary page in edit mode when all services are included and have data filled" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(MsbServices(
          Set(
            TransmittingMoney,
            CurrencyExchange,
            ChequeCashingNotScrapMetal,
            ChequeCashingScrapMetal
          )
        )),
        businessAppliedForPSRNumber = completedMT,
        ceTransactionsInNext12Months = completedCE
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.SummaryController.get().url))
    }

    "Navigate to Summary page in edit mode when CE pages are included and have data filled" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(MsbServices(
          Set(
            CurrencyExchange,
            ChequeCashingNotScrapMetal,
            ChequeCashingScrapMetal
          )
        )),
        ceTransactionsInNext12Months = completedCE
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.SummaryController.get().url))
    }

    "Navigate to MT section in edit mode when MT data is not in the store" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(MsbServices(
          Set(
            TransmittingMoney,
            CurrencyExchange,
            ChequeCashingNotScrapMetal,
            ChequeCashingScrapMetal
          )
        )),
        ceTransactionsInNext12Months = completedCE
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.BusinessAppliedForPSRNumberController.get().url))
    }

    "Navigate to CE section in edit mode when CE data is not in the store" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody (
        "linkedTxn" -> "true"
      )

      val incomingModel = MoneyServiceBusiness(
        msbServices = Some(MsbServices(
          Set(
            TransmittingMoney,
            CurrencyExchange,
            ChequeCashingNotScrapMetal,
            ChequeCashingScrapMetal
          )
        )),
        businessAppliedForPSRNumber = completedMT
      )

      when(controller.dataCacheConnector.fetch[MoneyServiceBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(incomingModel)))

      when(controller.dataCacheConnector.save[MoneyServiceBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.msb.routes.CETransactionsInNext12MonthsController.get().url))
    }
  }
}
