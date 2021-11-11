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


trait KucoinService[F[_]]{
  def getAccounts(n: KucoinService.Name): F[KucoinService.AccountResponse]
}

object KucoinService extends StrictLogging with AuthenticationHeaders {
  implicit def apply[F[_]](implicit ev: KucoinService[F]): KucoinService[F] = ev

  final case class Name(name: String) extends AnyVal
  final case class ApiError(e: Throwable) extends RuntimeException
  /**
    * More generally you will want to decouple your edge representations from
    * your internal data structures, however this shows how you can
    * create encoders for your data.
    **/
  final case class AccountResponse(code: String, accounts: List[Account])
  final case class TimeResponse(code: String, data: BigInt)
  final case class Account(id: String, currency: String, balance: String, available: String, holds: String)

  object AccountResponse {

    // acciount
    implicit val accountdecoder: Decoder[Account] = deriveDecoder[Account]
    implicit def accountEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Account] = jsonOf

    implicit val accountEncoder: Encoder[Account] = deriveEncoder[Account]
    implicit def accountEntityEncoder[F[_]]: EntityEncoder[F, Account] =
      jsonEncoderOf

    // greeting
    implicit val decoder: Decoder[AccountResponse] = (hcursor: HCursor) => for {
      code <- hcursor.downField("code").as[String]
      data <- hcursor.downField("data").as[List[Account]]
    } yield AccountResponse(code, data)
    implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, AccountResponse] = jsonOf

    implicit val encoder: Encoder[AccountResponse] = deriveEncoder[AccountResponse]
    implicit def entityEncoder[F[_]]: EntityEncoder[F, AccountResponse] =
      jsonEncoderOf

    // TimeResponse
    implicit val timeRespDecoder: Decoder[TimeResponse] = deriveDecoder[TimeResponse]
    implicit def timeRespEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, TimeResponse] = jsonOf

  }

  def impl[F[_]: Concurrent](C: Client[F]): KucoinService[F] = new KucoinService[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._

    def getAccounts(n: KucoinService.Name): F[KucoinService.AccountResponse] = {
      val requestType = "GET"
      val uri = "https://api.kucoin.com"
      val endpoint = "/api/v1/accounts?currency=BTC"
      val headers = getKucoinHeaders(requestType, endpoint, "")
        C.expect[AccountResponse] (GET(getUri(uri + endpoint)).putHeaders(headers))
    }

  }
}