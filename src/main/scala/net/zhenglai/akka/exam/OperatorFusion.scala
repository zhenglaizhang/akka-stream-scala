package net.zhenglai.akka.exam

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Fusing}
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App {

  implicit val system       = ActorSystem("operator-fusion")
  implicit val materializer = ActorMaterializer()

  val double = (_: Int) * 2
  val flow   = Flow[Int].map(double).filter(_ > 500)
  val fused  = Fusing.aggressive(flow)

  Source.fromIterator { () => Iterator from 0 }
    .via(fused)
    .take(20)
    .runForeach(println)

  Source(List(1, 2, 3))
    .map(_ + 1).async
    .map(_ * 2)
    .to(Sink.ignore)

  system.scheduler.scheduleOnce(10 seconds) {
    materializer.shutdown()
    system.terminate()
  }
}
