package net.zhenglai.akka.http.client

import java.io.File

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global

import akka.stream.scaladsl.{FileIO, Framing}
import akka.util.ByteString

object simple extends App {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  def transformEachLine(line: ByteString): ByteString = ByteString("transformed: ") ++ line

  responseFuture.map(x => {
    println(x);
    x.entity.dataBytes
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256))
      .map(transformEachLine)
      .runWith(FileIO.toPath(new File("/tmp/example.out").toPath))
  }).onComplete { _ =>
    materializer.shutdown(); // But it is not a shutdown. It does not shut down anything it just stops accepting newly materialized streams. The running streams can still go on forever.

    system.terminate()
  }
}
