package controllers

import config.AMLSAuthConnector
import controllers.auth.AmlsRegime
import services.AmlsService
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait MainSummaryController extends FrontendController  with Actions {
  def onPageLoad = AuthorisedFor(AmlsRegime).async {
    implicit user =>
      implicit request =>
      Future.successful(Ok(views.html.main_summary()))
  }
}

object MainSummaryController extends MainSummaryController {
  val amlsService = AmlsService
  val authConnector = AMLSAuthConnector
}
