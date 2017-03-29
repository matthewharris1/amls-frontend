package controllers.renewal

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import cats.implicits._
import connectors.DataCacheConnector
import controllers.BaseController
import models.registrationprogress.Completed
import play.api.i18n.MessagesApi
import services.{ProgressService, RenewalService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.renewal.renewal_progress

import scala.concurrent.Future

@Singleton
class RenewalProgressController @Inject()
(
  val authConnector: AuthConnector,
  dataCacheConnector: DataCacheConnector,
  progressService: ProgressService,
  messages: MessagesApi,
  renewals: RenewalService
) extends BaseController {

  def get() = Authorised.async {
    implicit authContext =>
      implicit request =>
        renewals.getSection flatMap { renewalSection =>

          val canSubmit = renewalSection.status == Completed

          val block = for {
            cache <- OptionT(dataCacheConnector.fetchAll)
          } yield {
            val variationSections = progressService.sections(cache)

            Ok(renewal_progress(renewalSection, variationSections, canSubmit))
          }

          block getOrElse Ok(renewal_progress(renewalSection, Seq.empty, canSubmit))
        }
  }

  def post() = Authorised.async {
    implicit authContext =>
      implicit request =>

      Future.successful(Redirect(controllers.declaration.routes.WhoIsRegisteringController.get()))
  }

}
