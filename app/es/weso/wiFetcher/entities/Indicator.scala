package es.weso.wiFetcher.entities

import es.weso.wiFetcher.entities.IndicatorType._
import es.weso.wiFetcher.entities.IndicatorHighLow._
import java.util.Date
import es.weso.wiFetcher.entities.traits.Component
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

case class Indicator(
  val id: String = null,
  val indicatorType: IndicatorType = null,
  val labels: HashMap[String, String] = new HashMap[String, String](),
  val comments: HashMap[String, String] = new HashMap[String, String](),
  var intervalStarts: Int = 0,
  var interfalFinishes: Int = 0,
  var countriesCoverage: Int = 0,
  val weight: Double = 0.0,
  val highLow: IndicatorHighLow = null,
  val source: String = "",
  val component: Component = null,
  val providers:ListBuffer[Provider] = ListBuffer.empty,
  val republish : Boolean) {

  override def equals(o: Any) = o match {
    case that: Indicator => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode(): Int = {
    id.hashCode()
  }

}