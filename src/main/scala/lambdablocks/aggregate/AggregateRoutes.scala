package lambdablocks.aggregate

import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import lambdablocks.aggregate.kucoin.KucoinService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object AggregateRoutes extends StrictLogging{

  def jokeRoutes[F[_]: Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- J.get
          resp <- Ok(joke)
        } yield resp
    }
  }

  def kucoinServiceRoutes[F[_]: Sync](H: KucoinService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "accounts" / name =>
        Ok(H.getAccounts().handleError(err => {
          logger.error(err.getMessage, err)
          logger.info(name)
          KucoinService.AccountResponse("500 error" , Nil)
        }))
    }
  }
}