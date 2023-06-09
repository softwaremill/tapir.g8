package $package$

import sttp.tapir.server.netty.{NettyFutureServer, NettyFutureServerOptions}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import ExecutionContext.Implicits.global

@main def run(): Unit =

  val serverOptions = NettyFutureServerOptions.customiseInterceptors
    .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
    .options

  val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)
  val program =
    for
      binding <- NettyFutureServer(serverOptions).port(port).addEndpoints(Endpoints.all).start()
      _ <- Future {
        println(s"Go to http://localhost:\${binding.port}/docs to open SwaggerUI. Press ENTER key to exit.")
        StdIn.readLine()
      }
      stop <- binding.stop()
    yield stop

  Await.result(program, Duration.Inf)
