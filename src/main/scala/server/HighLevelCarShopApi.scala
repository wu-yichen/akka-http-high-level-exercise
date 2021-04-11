package server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import jsonUtil.JsonSupport
import model.Car
import persistence.CarDb
import persistence.CarDb.{AddCar, FindAllCars, FindCarsById, IsInStock}
import server.CarShop.dataSetup
import spray.json._

import scala.concurrent.duration._
import scala.language.postfixOps

/** Get /api/car fetches all the cars in the db
  * Get /api/car?id=x fetches the car with id x
  * Get /api/car/X fetches cars with id x
  * Get /api/car/inventory?inStock=true
  */
object HighLevelCarShopApi extends App with JsonSupport {
  implicit val system: ActorSystem = ActorSystem("CarShop")
  implicit val materializer: ActorMaterializer.type = ActorMaterializer
  implicit val timeOut: Timeout = Timeout(3 seconds)
  import system.dispatcher

  val dbActor = dataSetup(system)

  val carServerRoute =
    (pathPrefix("api" / "car") & get) {
      path("inventory") {
        parameter(Symbol("inStock").as[Boolean]) { condition =>
          complete(
            (dbActor ? IsInStock(condition))
              .mapTo[Seq[Car]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          )
        }
      } ~
        (path(IntNumber) | parameter(Symbol("id").as[Int])) { id =>
          complete(
            (dbActor ? FindCarsById(id))
              .mapTo[Option[Car]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          )
        } ~
        pathEndOrSingleSlash {
          complete(
            (dbActor ? FindAllCars)
              .mapTo[Seq[Car]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
          )
        }
    }

  Http().newServerAt("localhost", 8080).bindFlow(carServerRoute)

  private def toHttpEntity(content: String) =
    HttpEntity(
      ContentTypes.`application/json`,
      content
    )
}

object CarShop {
  def dataSetup(system: ActorSystem): ActorRef = {
    val dbActor: ActorRef = system.actorOf(Props[CarDb])
    val cars = Seq(Car("Honda"), Car("Toyota"), Car("Volkswagen"))
    cars.foreach(dbActor ! AddCar(_))
    dbActor
  }
}
