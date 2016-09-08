package controllers.businessmatching

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import models.businessmatching.BusinessMatching
import views.html.businessmatching._

trait SummaryController extends BaseController {

  protected def dataCache: DataCacheConnector

  def get(completed: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCache.fetch[BusinessMatching](BusinessMatching.key) map {
        case Some(data) => Ok(summary(data))
        case _ => Redirect(controllers.routes.RegistrationProgressController.get())
      }
  }
}

object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override val dataCache = DataCacheConnector
  override val authConnector = AMLSAuthConnector
}
