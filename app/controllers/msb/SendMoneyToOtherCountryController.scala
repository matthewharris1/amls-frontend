package controllers.msb

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.moneyservicebusiness.{CurrencyExchange, MoneyServiceBusiness, MsbService, SendMoneyToOtherCountry}
import play.api.mvc.Result
import play.mvc.Results.Redirect
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.msb.send_money_to_other_country

import scala.concurrent.Future

trait SendMoneyToOtherCountryController extends BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(edit:Boolean = false) = Authorised.async {
   implicit authContext => implicit request =>
     dataCacheConnector.fetch[MoneyServiceBusiness](MoneyServiceBusiness.key) map {
       response =>
         val form: Form2[SendMoneyToOtherCountry] = (for {
           msb <- response
           money <- msb.sendMoneyToOtherCountry
         } yield Form2[SendMoneyToOtherCountry](money)).getOrElse(EmptyForm)
         Ok(send_money_to_other_country(form, edit))
     }
  }

  private def standardRouting(next: Boolean, services: Set[MsbService]): Result =
    (next, services) match {
      case (true, _) =>
        Redirect(routes.SendTheLargestAmountsOfMoneyController.get())
      case (false, s) if s contains CurrencyExchange =>
        Redirect(routes.CETransactionsInNext12MonthsController.get())
      case (false, _) =>
        Redirect(routes.SummaryController.get())
    }

  private def editRouting(next: Boolean, services: Set[MsbService], msb: MoneyServiceBusiness): Result =
    (next: Boolean, services) match {
      case (true, _) if !msb.sendTheLargestAmountsOfMoney.isDefined =>
        Redirect(routes.SendTheLargestAmountsOfMoneyController.get(true))
      case (false, s)
        if (s contains CurrencyExchange) && !msb.sendTheLargestAmountsOfMoney.isDefined =>
          Redirect(routes.CETransactionsInNext12MonthsController.get(true))
      case _ =>
        Redirect(routes.SummaryController.get())
    }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[SendMoneyToOtherCountry](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(send_money_to_other_country(f, edit)))
        case ValidForm(_, data) =>
          for {
            msb <- dataCacheConnector.fetch[MoneyServiceBusiness](MoneyServiceBusiness.key)
            _ <- dataCacheConnector.save[MoneyServiceBusiness](MoneyServiceBusiness.key,
              msb.sendMoneyToOtherCountry(data)
            )
          } yield {

            val services = msb.msbServices.map(_.services).getOrElse(Set.empty)

            edit match {
              case true =>
                editRouting(data.money, services, msb)
              case false =>
                standardRouting(data.money, services)
            }
          }
      }
    }
  }
}

object SendMoneyToOtherCountryController extends SendMoneyToOtherCountryController {
  // $COVERAGE-OFF$
  override val dataCacheConnector: DataCacheConnector = DataCacheConnector
  override protected def authConnector: AuthConnector = AMLSAuthConnector
}
