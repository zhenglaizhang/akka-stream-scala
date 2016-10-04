package net.zhenglai.akka.http

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.stream.ActorMaterializer
import scala.language.postfixOps
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import scala.io.StdIn
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.util.{Timeout}


object webbid extends App {

  final case class Bid(userId: String, bid: Int)

  case object GetBids

  case class Bids(bids: List[Bid])

  class Auction extends Actor {
    private val bids = scala.collection.mutable.ArrayBuffer[Bid]()

    override def receive: Receive = {
      case b@Bid(userId, bid) =>
        bids.append(b)
      case GetBids =>
        sender() ! Bids(bids.toList)
    }
  }

  object Auction {
    def props = Props[Auction]()
  }

  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)


  implicit val system = ActorSystem("bidding-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val auction = system.actorOf(Auction.props, "auction")

  val route =
    path("auction") {
      post {
        //        parameter("bid".as[Int], "user") { (bid, user) =>
        entity(as[Bid]) { bid =>
          // place a bid, fire-and-forget
          auction ! bid
          complete((StatusCodes.Accepted, "bid placed"))
        }
      } ~
        get {
          implicit val timeout: Timeout = 5 seconds
          val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
          complete(bids)
        }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
