package lambdablocks.aggregate

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    AggregateServer.stream[IO].compile.drain.as(ExitCode.Success)
}
