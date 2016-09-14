package controllers.businessmatching

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.businessmatching.{TransmittingMoney, MsbServices, BusinessMatching}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

trait ServicesController extends BaseController {

  def cache: DataCacheConnector

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      cache.fetch[BusinessMatching](BusinessMatching.key) map {
        response =>

          val form = (for {
            msb <- response
            services <- msb.msbServices
          } yield Form2[MsbServices](services)).getOrElse(EmptyForm)

          Ok(views.html.msb.services(form, edit))
      }
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      Form2[MsbServices](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(views.html.msb.services(f, edit)))
        case ValidForm(_, data) =>
          for {
            msb <- cache.fetch[BusinessMatching](BusinessMatching.key)
             _ <- cache.save[BusinessMatching](BusinessMatching.key,
              msb.msbServices(data)
            )
          } yield data.services.contains(TransmittingMoney) match {
            case true =>
              Redirect(routes.SummaryController.get())
            case true =>
              Redirect(routes.SummaryController.get())
          }
      }
  }
}

object ServicesController extends ServicesController {
  // $COVERAGE-OFF$
  override protected def authConnector: AuthConnector = AMLSAuthConnector
  override val cache = DataCacheConnector
}
