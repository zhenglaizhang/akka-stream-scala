package net.zhenglai.akka.http.client

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global

object simple extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  responseFuture.map(x => {
    println(x);
    x
  }).onComplete(_ => system.terminate())
}
