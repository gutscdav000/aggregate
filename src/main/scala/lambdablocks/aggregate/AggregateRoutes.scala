package lambdablocks.aggregate

import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
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

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    import cats.syntax.applicativeError._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        Ok(H.getUser(HelloWorld.Name(name)).handleError(err => {
          logger.error(err.getMessage, err)
          HelloWorld.Greeting("500 error")
        }))
    }
  }
}