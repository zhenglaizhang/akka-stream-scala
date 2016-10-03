package net.zhenglai.akka.exam


import scala.concurrent.Future

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import net.zhenglai.akka.model.{Author, Hashtag, Tweet}

object ReactiveTweets extends App {

  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  val dummy = Seq(
    Tweet(Author("zhenglai"), System.currentTimeMillis, "#tag hello #twee"),
    Tweet(Author("zhenglai1"), System.currentTimeMillis, "#tag hello #akka"),
    Tweet(Author("zhenglai2"), System.currentTimeMillis, "#tag hello #akka"),
    Tweet(Author("zhenglai3"), System.currentTimeMillis, "#tag hello #twee"),
    Tweet(Author("zhenglai3"), System.currentTimeMillis, "#tag hello #twee"),
    Tweet(Author("zhenglai3"), System.currentTimeMillis, "#tag hello #twee"),
    Tweet(Author("zhenglai3"), System.currentTimeMillis, "#tag hello #twee"),
    Tweet(Author("zhenglai zhang"), System.currentTimeMillis, "#tag hello #akka")
  )
  println(dummy.getClass)

  val tweets: Source[Tweet, NotUsed] = Source[Tweet](dummy.to[scala.collection.immutable.Iterable])

  val authors: Source[Author, NotUsed] =
    tweets
      .buffer(2, OverflowStrategy.dropHead)
      .filter(_.hashtags.contains(Hashtag.akka))
      .map(_.author)

  authors.runForeach(println)


  println("=" * 20)


  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map(_ => 1)
  val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
  val counterGraph: RunnableGraph[Future[Int]] =
    tweets
      .via(count)
      .toMat(sumSink)(Keep.right)

  val sum: Future[Int] = counterGraph.run()
  sum.foreach(c => println(s"Total tweets processed: $c"))

  val akkaCounterGraph: RunnableGraph[Future[Int]] =
    tweets
      .filter(_.hashtags contains Hashtag.akka)
      .map(_ => 1)
      .toMat(sumSink)(Keep.right)
  akkaCounterGraph.run().foreach(c => println(s"Total akka tweets processed: $c"))

  tweets
    .filter(_.hashtags contains Hashtag.akka)
    .map(_ => 1)
    .runWith(sumSink)
    .foreach(c => println(s"Total akka tweets by `runWith` processed: $c"))


  println("=" * 20)

  Source(1 to 10).toMat(Sink.fold(0)(_ + _))(Keep.right).run().foreach(s => println(s"sum is $s"))
  Source(1 to 10).runFold(0)(_ + _).foreach(s => println(s"sum is $s"))
  Source(1 to 10).runWith(Sink.fold(0)(_ + _)).foreach(s => println(s"sum is $s"))


  Source(List(1, 2, 3))
  Source.fromFuture(Future.successful("Hello Streams!"))
  Source.single("only one element")
  Source.empty
  //  Sink.fold(0)(_ + _)
  Sink.fold[Int, Int](0)(_ + _)
  Sink.head
  Sink.ignore

  // a sink executing a side-effecting call for every element of the stream
  Sink.foreach[Int](println(_))
  Sink.foreach[Int](println)

  println("=" * 20)

  // explicitly creating and wiring up a source, sink and flow
  Source(1 to 6).via(Flow[Int].map(_ * 2)).to(Sink.foreach(println)).run()

  val sep = () => println("=" * 20)

  sep()
  val sink: Sink[Int, NotUsed] = Flow[Int].map(_ * 2).to(Sink.foreach(println))
  Source(1 to 6).to(sink).run()

  sep()
  // broadcast to a sink inline
  val otherSink = Flow[Int].alsoTo(Sink.foreach(n => println(s"2 => $n"))).to(Sink.foreach(n => println(s" 1 => $n")))
  Source(20 to 30).to(otherSink).run()

  Thread.sleep(1000)
  materializer.shutdown()
  system.terminate()
  sys.exit(0)
}
