package lambdablocks.aggregate

import cats.effect.Sync
//import lambdablocks.aggregate.kucoin.ApiError
//import lambdablocks.aggregate.utils.HttpErrorHandler
//import cats.implicits._
import lambdablocks.aggregate.kucoin.{KucoinService}
import com.typesafe.scalalogging.StrictLogging
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object AggregateRoutes extends StrictLogging{

  def kucoinServiceRoutes[F[_]](S: KucoinService[F])(implicit F: Sync[F]/*, H: HttpErrorHandler[F, ApiError]*/): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    val routes = HttpRoutes.of[F] {
      case GET -> Root / "accounts" => Ok(S.getAccounts())
      case GET -> Root / "orders" => Ok(S.getOrders())
    }
    routes
//    H.handle(routes)
  }
}