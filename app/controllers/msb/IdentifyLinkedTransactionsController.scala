package controllers.msb

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.businessmatching.{TransmittingMoney, CurrencyExchange, MsbService, BusinessMatching}
import models.moneyservicebusiness._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.msb.identify_linked_transactions

import scala.concurrent.Future

trait IdentifyLinkedTransactionsController extends BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetch[MoneyServiceBusiness](MoneyServiceBusiness.key) map {
        response =>
          val form: Form2[IdentifyLinkedTransactions] = (for {
            msb <- response
            transactions <- msb.identifyLinkedTransactions
          } yield Form2[IdentifyLinkedTransactions](transactions)).getOrElse(EmptyForm)
          Ok(identify_linked_transactions(form, edit))
      }
  }

  private def standardRouting(services: Set[MsbService]): Result =
    services match {
      case s if s contains TransmittingMoney =>
        Redirect(routes.BusinessAppliedForPSRNumberController.get())
      case s if s contains CurrencyExchange =>
        Redirect(routes.CETransactionsInNext12MonthsController.get())
      case _ =>
        Redirect(routes.SummaryController.get())
    }

  private def editRouting(services: Set[MsbService], msb: MoneyServiceBusiness): Result =
    services match {
      case s if s contains TransmittingMoney =>
        mtRouting(services, msb)
      case s if s contains CurrencyExchange =>
        ceRouting(msb)
      case _ =>
        Redirect(routes.SummaryController.get())
    }

  private def mtRouting(services: Set[MsbService], msb: MoneyServiceBusiness): Result =
    if (msb.businessAppliedForPSRNumber.isDefined) {
      editRouting(services - TransmittingMoney, msb)
    } else {
      Redirect(routes.BusinessAppliedForPSRNumberController.get(true))
    }

  private def ceRouting(msb: MoneyServiceBusiness): Result =
    if (msb.ceTransactionsInNext12Months.isDefined) {
      Redirect(routes.SummaryController.get())
    } else {
      Redirect(routes.CETransactionsInNext12MonthsController.get(true))
    }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[IdentifyLinkedTransactions](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(identify_linked_transactions(f, edit)))
        case ValidForm(_, data) =>
          dataCacheConnector.fetchAll flatMap {
            optMap =>
              val result = for {
                cache <- optMap
                msb <- cache.getEntry[MoneyServiceBusiness](MoneyServiceBusiness.key)
                bm <- cache.getEntry[BusinessMatching](BusinessMatching.key)
                services <- bm.msbServices
              } yield {
                dataCacheConnector.save[MoneyServiceBusiness](MoneyServiceBusiness.key,
                  msb.identifyLinkedTransactions(data)
                ) map {
                  _ =>
                    if (edit) {
                      editRouting(services.services, msb)
                    } else {
                      standardRouting(services.services)
                    }
                }
              }
              result getOrElse Future.failed(new Exception("Unable to retrieve sufficient data"))
          }
      }
    }
  }
}

object IdentifyLinkedTransactionsController extends IdentifyLinkedTransactionsController {
  // $COVERAGE-OFF$
  override val dataCacheConnector: DataCacheConnector = DataCacheConnector

  override protected def authConnector: AuthConnector = AMLSAuthConnector
}
