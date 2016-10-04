package net.zhenglai.akka.exam

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object MapAsync extends App {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()

  type UserId = String
  type FullName = String

  def databaseLookup(userId: UserId): FullName = {
    Thread.sleep(10000)
    "user_id_after_long_querying"
  }

  val userIdSource: Source[UserId, NotUsed] = ???

  val stream = userIdSource
    .via(Flow[UserId].map(databaseLookup))
    .to(Sink.foreach(println))
    .run()
  // One limitation is that this stream will only make 1 db query at a time


  /*
  "Stream transformations and side effects involving external non-stream based services can be performed with mapAsync or mapAsyncUnordered"

  The difference is best highlighted in the signatures:  Flow.map takes in a function that returns a type T while Flow.mapAsync takes in a function that returns a type Future[T].
   */

  def concurrentDbLookup(userId: UserId): Future[FullName] = Future {
    databaseLookup(userId)
  }


  /*
The problem with this simplistic concurrency addendum is that we have effectively eliminated backpressure. Because the Sink is just pulling in the Future and adding a foreach println it will continuously propagate demand and spawn off more Futures. This means that there is no limit to the number of databaseLookup running concurrently which would swamp the database. */
  val streamSwamped = userIdSource
    .via(Flow[UserId].map(concurrentDbLookup))
    .to(Sink.foreach[Future[FullName]](_ foreach println))
    .run()


  /*
  Flow.mapAsync to the rescue; we can have concurrent db lookups while at the same time limiting the number of connections:
   */
  val maxLookupCount = 10
  val streamAsync    = userIdSource
    .via(Flow[UserId].mapAsync(maxLookupCount)(concurrentDbLookup))
    .to(Sink.foreach(println))
    .run()

  /*
  If you don't care about maintaining ordering of the UserIDs to FullNames you can use Flow.mapAsyncUnordered.
   */

  system.terminate()
}
