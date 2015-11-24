package controllers.aboutTheBusiness

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.AMLSGenericController
import models.BusinessHasWebsite
import forms.AboutTheBusinessForms._
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

trait BusinessHasWebsiteController extends AMLSGenericController{

  val dataCacheConnector: DataCacheConnector = DataCacheConnector
  val CACHE_KEY = "businessHasWebsite"
  override def get(implicit user: AuthContext, request: Request[AnyContent]): Future[Result] = {
    dataCacheConnector.fetchDataShortLivedCache[BusinessHasWebsite](CACHE_KEY) map {
      case Some(data) => Ok(views.html.businessHasWebsite(businessHasWebsiteForm.fill(data)))
      case _ => Ok(views.html.businessHasWebsite(businessHasWebsiteForm))
    }
  }

  override def post(implicit user: AuthContext, request: Request[AnyContent]): Future[Result] =
    businessHasWebsiteForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(views.html.businessHasWebsite(errors))),
      details => {
        dataCacheConnector.saveDataShortLivedCache[BusinessHasWebsite](CACHE_KEY, details) map { _=>
          NotImplemented("Not implemented")
        }
      })

}

object BusinessHasWebsiteController extends BusinessHasWebsiteController {
   override val authConnector: AuthConnector = AMLSAuthConnector
   override val dataCacheConnector: DataCacheConnector = DataCacheConnector
}

