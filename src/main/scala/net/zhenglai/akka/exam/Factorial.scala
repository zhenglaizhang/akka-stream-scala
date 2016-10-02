package net.zhenglai.akka.exam

import java.nio.file.Paths

import scala.concurrent.Future

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString

object Factorial extends App {

  def lineSink(fileName: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s"$s\n"))
      .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)

  implicit val actorSystem = ActorSystem("quick-start");
  implicit val materializer = ActorMaterializer()
  val source: Source[Int, NotUsed] = Source(1 to 100)
  source.runForeach(println)

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  factorials.map(_.toString).runWith(lineSink("factorials2.txt"))


  println("DONE...")
}
