package persistence

import akka.actor.{Actor, ActorLogging}
import model.Car
import persistence.CarDb._

import scala.collection.mutable

class CarDb extends Actor with ActorLogging {

  val db = mutable.Map.empty[Int, Car]

  var id = 0

  override def receive: Receive = {

    case AddCar(car) =>
      db += (id -> car)
      log.info(s"Car $car has been added with id = $id")
      sender() ! CarAdded(id)
      id += 1

    case FindAllCars => sender() ! db.values.toSeq

    case FindCarsById(id) =>
      val maybeCar = db.get(id)
      sender() ! maybeCar

    case IsInStock(is) =>
      if (is)
        sender() ! db.filter(item => item._2.quantity > 0).values.toSeq
      else
        sender() ! db.filter(item => item._2.quantity == 0).values.toSeq
  }
}

object CarDb {

  case class AddCar(car: Car)

  case class CarAdded(id: Int)

  case class FindCarsById(id: Int)

  case object FindAllCars

  case class IsInStock(is: Boolean)
}
