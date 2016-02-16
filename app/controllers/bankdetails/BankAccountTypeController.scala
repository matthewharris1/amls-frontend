package controllers.bankdetails

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{ValidForm, InvalidForm, EmptyForm, Form2}
import models.bankdetails.{NoBankAccount, BankAccountType, BankDetails}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait BankAccountTypeController extends BankAccountUtilController {

  val dataCacheConnector : DataCacheConnector

  def get(index:Int = 0, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      getBankDetails(index) map {
        case Some(BankDetails(Some(data), _)) => Ok(views.html.bank_account_types(Form2[BankAccountType](data), edit, index))
        case _ => Ok(views.html.bank_account_types(EmptyForm, edit, index))
      }
  }

  def post(index:Int = 0, edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      Form2[BankAccountType](request.body) match {
        case f: InvalidForm => Future.successful(BadRequest(views.html.bank_account_types(f, edit, index)))
        case ValidForm(_, data) => {
          for {
            result <- updateBankDetails(index, BankDetails(Some(data), None))
          } yield {
            data match {
              case NoBankAccount => Redirect(routes.WhatYouNeedController.get())
              case _ => Redirect(routes.BankAccountTypeController.get())
            }
          }
        }
      }
    }
  }
}

object BankAccountTypeController extends BankAccountTypeController {
    override val authConnector = AMLSAuthConnector
    override val dataCacheConnector = DataCacheConnector
}