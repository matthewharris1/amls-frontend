package modules

import com.google.inject.AbstractModule
import config.WSHttp
import connectors.{DataCacheConnector, KeystoreConnector}
import services.{ProgressService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HttpPost

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[HttpPost]).toInstance(WSHttp)
    bind(classOf[KeystoreConnector]).toInstance(KeystoreConnector)
    bind(classOf[DataCacheConnector]).toInstance(DataCacheConnector)
    bind(classOf[AuthConnector]).to(classOf[config.FrontendAuthConnector])
    bind(classOf[ProgressService]).toInstance(ProgressService)

  }
}
