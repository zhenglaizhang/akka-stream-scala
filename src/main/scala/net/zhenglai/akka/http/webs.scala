package net.zhenglai.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import scala.io.StdIn
import scala.concurrent.Future

object webs extends App {

  // domain model
  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  // format for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = Future.successful(Some(Item("book", 12L)))

  def saveOrder(order: Order): Future[Done] = Future.successful(Done)


  // needed to run the route
  implicit val system = ActorSystem("web-server")
  implicit val materializer = ActorMaterializer()


  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        //        complete {
        //          <h1>Hello Akka Http</h1>
        //        }
      }
    } ~
      get {
        pathPrefix("item" / LongNumber) { id =>
          val mayBeItem: Future[Option[Item]] = fetchItem(id)

          onSuccess(mayBeItem) {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      post {
        path("create-order") {
          entity(as[Order]) { order =>
            val saved: Future[Done] = saveOrder(order)
            onComplete(saved) { done =>
              complete("order created")
            }
          }
        }
      } ~
      path("random") {
        get {
          complete()
        }
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080\nPress enter to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
