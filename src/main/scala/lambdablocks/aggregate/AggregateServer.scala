package lambdablocks.aggregate

//import cats.MonadError
//import lambdablocks.aggregate.utils.HttpErrorHandler
//import org.http4s.implicits._
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import lambdablocks.aggregate.kucoin.{KucoinClient, KucoinService}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object AggregateServer {
  def stream[F[_]: Async]: Stream[F, Nothing] = {
//    implicit def appHttpErrorHandler: HttpErrorHandler[F, ApiError] = new AppHttpErrorHandler[F]
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      // CLIENTS object (when more than 1)
      kucoinClient = KucoinClient.impl[F](client)
      // SERVICES object (when more than 1)
      kucoinServiceAlg = KucoinService.impl[F](kucoinClient)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        AggregateRoutes.kucoinServiceRoutes[F](kucoinServiceAlg) //<+>
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
