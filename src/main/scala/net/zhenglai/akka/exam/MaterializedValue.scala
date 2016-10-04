package net.zhenglai.akka.exam

import scala.concurrent.{Future, Promise}

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

object MaterializedValue extends App {

  implicit val system = ActorSystem("combining-materialized-value")
  implicit val materializer = ActorMaterializer()

  // a source that can be signalled explicitly from the outside
  val source: Source[Int, Promise[Option[Int]]] = Source.maybe[Int]

  //  val flow: Flow[Int, Int, Cancellable] = Flow[Int].throttle(1, 1 second, 1, ThrottleMode.shaping).map(_ * 2)
  /*
  val flow: Flow[Int, Int, Cancellable] = _

  val sink: Sink[Int, Future[Int]] = Sink.head[Int]

  val r1: RunnableGraph[Promise[Option[Int]]] = source.via(flow).to(sink)
  val r2: RunnableGraph[Cancellable] = source.viaMat(flow)(Keep.right).to(sink)
  val r3: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  val r4: Future[Int] = source.via(flow).runWith(sink)
  val r5: Promise[Option[Int]] = flow.to(sink).runWith(source)
  val r6: (Promise[Option[Int]], Future[Int]) = flow.runWith(source, sink)

  val r7: RunnableGraph[(Promise[Option[Int]], Cancellable)] =
    source.viaMat(flow)(Keep.both).to(sink)

  val r8: RunnableGraph[(Promise[Option[Int]], Future[Int])] =
    source.via(flow).toMat(sink)(Keep.both)
  val r9: RunnableGraph[((Promise[Option[Int]], Cancellable), Future[Int])] =
    source.viaMat(flow)(Keep.both).toMat(sink)(Keep.both)
  val r10: RunnableGraph[(Cancellable, Future[Int])] =
    source.viaMat(flow)(Keep.right).toMat(sink)(Keep.both):w

*/
}
