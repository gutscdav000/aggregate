package lambdablocks.aggregate.kucoin

import cats.effect.Concurrent
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, HCursor}
import lambdablocks.aggregate.utils.AuthenticationHeaders
import org.http4s._
import org.http4s.circe._

trait KucoinService[F[_]] {
  def getAccounts(): F[KucoinService.AccountResponse]
}

object KucoinService extends StrictLogging with AuthenticationHeaders {
  implicit def apply[F[_]](implicit ev: KucoinService[F]): KucoinService[F] = ev

  final case class ApiError(e: Throwable) extends RuntimeException
  final case class Account(id: String, currency: String, balance: String, available: String, holds: String)
  final case class AccountResponse(code: String, accounts: List[Account])

  object AccountResponse {
    // account
    implicit val accountdecoder: Decoder[Account] = deriveDecoder[Account]
    implicit def accountEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Account] = jsonOf

    implicit val accountEncoder: Encoder[Account] = deriveEncoder[Account]
    implicit def accountEntityEncoder[F[_]]: EntityEncoder[F, Account] =
      jsonEncoderOf

    // AccountResponse
    implicit val decoder: Decoder[AccountResponse] = (hcursor: HCursor) => for {
      code <- hcursor.downField("code").as[String]
      data <- hcursor.downField("data").as[List[Account]]
    } yield AccountResponse(code, data)
    implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, AccountResponse] = jsonOf

    implicit val encoder: Encoder[AccountResponse] = deriveEncoder[AccountResponse]
    implicit def entityEncoder[F[_]]: EntityEncoder[F, AccountResponse] =
      jsonEncoderOf
  }

  def impl[F[_]](kucoinRepo: KucoinRepository[F]): KucoinService[F] = new KucoinService[F]{

    def getAccounts(): F[KucoinService.AccountResponse] = {
      val response: F[AccountResponse] = kucoinRepo.getAccounts()
      response
    }

  }
}