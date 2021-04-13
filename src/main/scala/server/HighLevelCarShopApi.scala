package server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import jsonUtil.JsonSupport
import model.Car
import persistence.CarDb
import persistence.CarDb.{AddCar, FindAllCars, FindCarsById, IsInStock}
import server.CarShop.dataSetup

import scala.concurrent.duration._
import scala.language.postfixOps

/** Get /api/car fetches all the cars in the db
  * Get /api/car?id=x fetches the car with id x
  * Get /api/car/X fetches cars with id x
  * Get /api/car/inventory?inStock=true
  */
object HighLevelCarShopApi extends App with JsonSupport with SprayJsonSupport {
  implicit val system: ActorSystem = ActorSystem("CarShop")
  implicit val materializer: ActorMaterializer.type = ActorMaterializer
  implicit val timeOut: Timeout = Timeout(3 seconds)

  val dbActor = dataSetup(system)

  val carServerRoute =
    (pathPrefix("api" / "car") & get) {
      path("inventory") {
        parameter(Symbol("inStock").as[Boolean]) { condition =>
          complete((dbActor ? IsInStock(condition)).mapTo[Seq[Car]])
        }
      } ~
        (path(IntNumber) | parameter(Symbol("id").as[Int])) { id =>
          complete((dbActor ? FindCarsById(id)).mapTo[Option[Car]])
        } ~
        pathEndOrSingleSlash {
          complete((dbActor ? FindAllCars).mapTo[Seq[Car]])
        }
    }

  Http().newServerAt("localhost", 8081).bindFlow(carServerRoute)
}

object CarShop {
  def dataSetup(system: ActorSystem): ActorRef = {
    val dbActor: ActorRef = system.actorOf(Props[CarDb])
    val cars = Seq(Car("Honda"), Car("Toyota"), Car("Volkswagen"))
    cars.foreach(dbActor ! AddCar(_))
    dbActor
  }
}
