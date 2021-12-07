package lambdablocks.aggregate.utils

//import cats._
//import cats.syntax.all._
//import cats.data.{Kleisli, OptionT}
import org.http4s.{HttpRoutes}

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

object HttpErrorHandler {
  def apply[F[_], E <: Throwable](implicit ev: HttpErrorHandler[F, E]) = ev
}

//object RoutesHttpErrorHandler {
//  def apply[F[_]: ApplicativeError[?[_], E], E <: Throwable](routes: HttpRoutes[F])(
//    handler: E => F[Response[F]]): HttpRoutes[F] =
//    Kleisli(req => OptionT(routes.run(req).value.handleErrorWith(e => handler(e).map(Option.apply))))
//}