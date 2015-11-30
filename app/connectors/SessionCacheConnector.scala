package connectors

import config.AmlsSessionCache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait SessionCacheConnector {

  def sessionCache: SessionCache

  def getReviewBusinessDetails[T](implicit hc: HeaderCarrier, formats: json.Format[T]): Future[T]

  def fetchAndGetData[T](key: String)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry[T](key)
  }
}

object SessionCacheConnector extends SessionCacheConnector {

  private val BCSourceId = "BC_Business_Details"

  override def sessionCache: SessionCache = AmlsSessionCache

  override def getReviewBusinessDetails[T](implicit hc: HeaderCarrier, formats: json.Format[T]): Future[T] = {
    fetchAndGetData[T](BCSourceId) flatMap {
      case Some(data) => Future.successful(data)
      case None => throw new RuntimeException("No Review Details Found")
    }
  }
}