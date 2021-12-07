//package lambdablocks.aggregate.kucoin
//
//import cats.data.{Kleisli, OptionT}
//import cats.{ApplicativeError, MonadError}
//import lambdablocks.aggregate.utils.{HttpErrorHandler}
//import org.http4s.{HttpRoutes, Response}
//import org.http4s.dsl.Http4sDsl
//
////import cats.effect._
////import cats.syntax.all._
////import cats.implicits._
////import cats.syntax.all._
//
//import cats._
//import cats.syntax.all._
//object RoutesHttpErrorHandler {
//  def apply[F[_]: ApplicativeError[?[_], E], E <: Throwable](routes: HttpRoutes[F])(
//    handler: E => F[Response[F]]): HttpRoutes[F] =
//    Kleisli(req => OptionT(routes.run(req).value.handleErrorWith(e => handler(e).map(Option.apply))))
//}
//
//class AppHttpErrorHandler[F[_]: MonadError[?[_], ApiError]] extends HttpErrorHandler[F, ApiError] with Http4sDsl[F] {
//  private val handler: ApiError => F[Response[F]] = {
//    case ApiError(msg) => InternalServerError(s"Generic ApiError $msg")
//    case _             => InternalServerError(s"error encountered is undocumented")
//  }
//
//  override def handle(routes: HttpRoutes[F]): HttpRoutes[F] = RoutesHttpErrorHandler(routes)(handler)
//}
