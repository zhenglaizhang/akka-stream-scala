package net.zhenglai.akka.http.low

import scala.concurrent.Future
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

object webs2 extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val (interface, port) = ("localhost", -1)
  val serverSource = Http().bind(interface, port)

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "<html><body>Hello world!</body></html>"))
    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      HttpResponse(entity = "PONG")
    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sys.error("BOOM!")
    case r: HttpRequest =>
      r.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound, entity = "Unknown resource!")
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach { connection =>
      println(s"Accepted new connection from ${connection.remoteAddress}")
      connection handleWithSyncHandler requestHandler
      // equivalent to
      // connection handleWith { Flow[HttpRequest] map requestHandler }
    }).run()

  bindingFuture.onFailure {
    case ex: Exception =>
      println(s"Failed to bind to $interface:$port")
  }

  Thread.sleep(20000)
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
