package net.zhenglai.akka.exam

import java.nio.file.Paths

import scala.concurrent.Future

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.stream.{ActorMaterializer, IOResult, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString

object Factorial extends App {

  def lineSink(fileName: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s"$s\n"))
      .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)

  implicit val system = ActorSystem("quick-start");
  implicit val materializer = ActorMaterializer()
  val source: Source[Int, NotUsed] = Source(1 to 100)

  // shortcut version
  source.runForeach(println)
  println("full version ...")
  Source(1 to 10).runWith(Sink.foreach(println))
  println("full version done...")

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("target/factorials.txt")))

  factorials.map(_.toString).runWith(lineSink("target/factorials2.txt"))


  println("DONE...")

  (1 to 10).foreach(println)

  val done: Future[Done] =
    factorials
      .zipWith(Source(0 to 20))((num, idx) => s"$idx! = $num")
      .throttle(1, 1 second, 1, ThrottleMode.shaping)
      .runForeach(println)

  Thread.sleep(10000)
  done.onComplete {
    _ => system.terminate()
  }
  println("DONE")
}
