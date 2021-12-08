package lambdablocks.aggregate.kucoin

import cats.effect.Concurrent
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, HCursor}
import lambdablocks.aggregate.utils.AuthenticationHeaders
import org.http4s._
import org.http4s.circe._

trait KucoinService[F[_]] {
  def getAccounts(): F[AccountResponse]
  def getOrders(): F[OrderResponse]
}

final case class ApiError(e: Throwable) extends RuntimeException
final case class Account(id: String, currency: String, balance: String, available: String, holds: String)
final case class AccountResponse(code: String, accounts: List[Account])
final case class Order(
          id: String, symbol: String, opType: String, /*type: String,*/ side: String,price: String,size: String, funds: String, dealFunds: String,
          dealSize: String,fee: String,feeCurrency: String,stp: String,stop: String,stopTriggered: String,stopPrice: String, timeInForce: String,
          postOnly: Boolean, hidden: Boolean, iceberg: Boolean, visibleSize: String, cancelAfter: Int, channel: String, clientOid: Option[String],
          remark: Option[String], tags: Option[String], isActive: Boolean, cancelExist: Boolean, createdAt: BigInt, tradeType: String )
final case class OrderPage(currentPage: Int, pageSize: Int, totalNum: Int, totalPage: Int, items: List[Order])
final case class OrderResponse(code: String, orders: List[Order])

object OrderResponse {
  implicit val orderdecoder: Decoder[Order] = deriveDecoder[Order]
  implicit def orderEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Order] = jsonOf

  implicit val orderEncoder: Encoder[Order] = deriveEncoder[Order]
  implicit def orderEntityEncoder[F[_]]: EntityEncoder[F, Order] =
    jsonEncoderOf

  implicit val orderPagedecoder: Decoder[OrderPage] = (hcursor: HCursor) => for {
    cur <- hcursor.downField("currentPage").as[Int]
    size <- hcursor.downField("pageSize").as[Int]
    num <- hcursor.downField("totalNum").as[Int]
    page <- hcursor.downField("totalPage").as[Int]
    items <- hcursor.downField("items").as[List[Order]]
  } yield OrderPage(cur, size, num, page, items)
  implicit def orderPageEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, OrderPage] = jsonOf

  implicit val orderPageEncoder: Encoder[OrderPage] = deriveEncoder[OrderPage]
  implicit def orderPageEntityEncoder[F[_]]: EntityEncoder[F, OrderPage] =
    jsonEncoderOf

  implicit val orderResponsedecoder: Decoder[OrderResponse] = (hcursor: HCursor) => for {
    code <- hcursor.downField("code").as[String]
    data <- hcursor.downField("data").as[OrderPage]
  } yield OrderResponse(code, data.items)
  implicit def orderResponseEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, OrderResponse] = jsonOf

  implicit val orderResponseEncoder: Encoder[OrderResponse] = deriveEncoder[OrderResponse]
  implicit def orderResponseEntityEncoder[F[_]]: EntityEncoder[F, OrderResponse] =
    jsonEncoderOf
}

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

object KucoinService extends StrictLogging with AuthenticationHeaders {
  implicit def apply[F[_]](implicit ev: KucoinService[F]): KucoinService[F] = ev



  def impl[F[_]](kucoinClient: KucoinClient[F]): KucoinService[F] = new KucoinService[F]{
    def getAccounts(): F[AccountResponse] = kucoinClient.getAccounts()
    def getOrders(): F[OrderResponse] = kucoinClient.getOrders()
  }
}