package lambdablocks.aggregate.kucoin

import cats.effect.Concurrent
import lambdablocks.aggregate.utils.AuthenticationHeaders
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

trait KucoinClient[F[_]] {
  def getAccounts(): F[AccountResponse]
  def getOrders(): F[OrderResponse]
}

object KucoinClient extends AuthenticationHeaders {
  def apply[F[_]](implicit kucoinRepo: KucoinClient[F]): KucoinClient[F] = kucoinRepo


  def impl[F[_]: Concurrent](C: Client[F]): KucoinClient[F] = new KucoinClient[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._

    def getAccounts(): F[AccountResponse] = {
      val requestType = "GET"
      val uri = "https://api.kucoin.com"
      val endpoint = "/api/v1/accounts?currency=BTC"
      val headers = getKucoinHeaders(requestType, endpoint, "")
      C.expect[AccountResponse] (GET(getUri(uri + endpoint)).putHeaders(headers))
    }

    def getOrders(): F[OrderResponse] = {
      val requestType = "GET"
      val uri = "https://api.kucoin.com"
      val endpoint = "/api/v1/orders"
      val headers = getKucoinHeaders(requestType, endpoint, "")
      C.expect[OrderResponse] (GET(getUri(uri + endpoint)).putHeaders(headers))
    }

  }
}