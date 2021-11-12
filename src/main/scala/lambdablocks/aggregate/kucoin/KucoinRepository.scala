package lambdablocks.aggregate.kucoin

import cats.effect.Concurrent
import lambdablocks.aggregate.utils.AuthenticationHeaders
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

trait KucoinRepository[F[_]] {
  def getAccounts(): F[KucoinService.AccountResponse]
}

object KucoinRepository extends AuthenticationHeaders {
  def apply[F[_]](implicit kucoinRepo: KucoinRepository[F]): KucoinRepository[F] = kucoinRepo


  def impl[F[_]: Concurrent](C: Client[F]): KucoinRepository[F] = new KucoinRepository[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._

    def getAccounts(): F[KucoinService.AccountResponse] = {
      val requestType = "GET"
      val uri = "https://api.kucoin.com"
      val endpoint = "/api/v1/accounts?currency=BTC"
      val headers = getKucoinHeaders(requestType, endpoint, "")
      val response: F[KucoinService.AccountResponse] = C.expect[KucoinService.AccountResponse] (GET(getUri(uri + endpoint)).putHeaders(headers))
      response
    }

  }
}