package lambdablocks.aggregate

import cats.effect.Concurrent
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.circe._
import org.http4s.Method._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import com.typesafe.scalalogging.StrictLogging
import lambdablocks.aggregate.utils.AuthenticationHeaders


trait HelloWorld[F[_]]{
  def getUser(n: HelloWorld.Name): F[HelloWorld.Greeting]
}

object HelloWorld extends StrictLogging with AuthenticationHeaders {
  implicit def apply[F[_]](implicit ev: HelloWorld[F]): HelloWorld[F] = ev

  final case class Name(name: String) extends AnyVal
  final case class ApiError(e: Throwable) extends RuntimeException
  /**
    * More generally you will want to decouple your edge representations from
    * your internal data structures, however this shows how you can
    * create encoders for your data.
    **/
  final case class Greeting(greeting: String) extends AnyVal
  final case class TimeResponse(code: String, data: BigInt)
  final case class Account(id: String, currency: String, balance: String, available: String, holds: String)

  object Greeting {

    // greeting
    implicit val decoder: Decoder[Greeting] = (hCursor: HCursor) =>
      for {
        name <- hCursor.downField("data").downField("currency").as[String]
      } yield Greeting(name)
    implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, Greeting] = jsonOf

    implicit val forecastEncoder: Encoder[Greeting] = deriveEncoder[Greeting]
    implicit def forecastEntityEncoder[F[_]]: EntityEncoder[F, Greeting] =
      jsonEncoderOf

    // TimeResponse
    implicit val timeRespDecoder: Decoder[TimeResponse] = deriveDecoder[TimeResponse]
    implicit def timeRespEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, TimeResponse] = jsonOf

  }

  def impl[F[_]: Concurrent](C: Client[F]): HelloWorld[F] = new HelloWorld[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._

    def getUser(n: HelloWorld.Name): F[HelloWorld.Greeting] = {
      val requestType = "GET"
      val uri = "https://api.kucoin.com"
      val endpoint = "/api/v1/accounts?currency=BTC"
      val headers = getKucoinHeaders(requestType, endpoint, "")
        C.expect[Greeting] (GET(getUri(uri + endpoint)).putHeaders(headers))
    }

  }
}