package lambdablocks.aggregate

import lambdablocks.aggregate.kucoin.{KucoinClient, KucoinService}
import org.http4s._
import munit.CatsEffectSuite
import org.http4s.client.Client
import cats.effect._
import cats.syntax.all._
import org.http4s.syntax.all._

class KucoinServiceSpec extends CatsEffectSuite {

  test("HelloWorld returns status code 200") {
    assertIO(getAccounts(happyApp).map(_.status) ,Status.Ok)
  }

  //TODO: refactor kucoinServiceRoutes
  // this breaks correctly because client returns 500, but server returns 200
  test("HelloWorld returns status code 500") {
    assertIO(getAccounts(sadApp).map(_.status) ,Status.InternalServerError)
  }

//  test("HelloWorld returns hello world message") {
//    assertIO(getAccounts.flatMap(_.as[String]), "{\"message\":\"Hello, world\"}")
//  }

  private val happyApp = HttpApp[IO] { case r =>
    Response[IO](Status.Ok).withEntity(r.body).pure[IO]
  }

  private val sadApp = HttpApp[IO] { case r =>
    Response[IO](Status.InternalServerError).withEntity(r.body).pure[IO]
  }

  private[this] def getAccounts(app: HttpApp[IO]): IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/accounts")
    val client: Client[IO] = Client.fromHttpApp(app)
    val kucoinService = KucoinService.impl[IO](KucoinClient .impl[IO](client))
    AggregateRoutes.kucoinServiceRoutes(kucoinService).orNotFound(getHW)
  }
}