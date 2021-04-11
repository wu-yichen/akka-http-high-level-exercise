package jsonUtil

import model.Car
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends DefaultJsonProtocol {
  implicit val guitarFormat: RootJsonFormat[Car] = jsonFormat2(Car)
}
