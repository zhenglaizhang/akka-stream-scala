package net.zhenglai.akka.http.low

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.stream.ActorMaterializer
import scala.language.postfixOps

import akka.Done
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import scala.io.StdIn
import scala.concurrent.Future
import scala.util.Random
import scala.concurrent.duration._

import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}

object webs extends App {
  implicit val system = ActorSystem("low-level-http")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

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
      r.discardEntityBytes() // import to drain incoming HTTP entity
      HttpResponse(404, entity = "Unknown resource!")
  }

  val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
