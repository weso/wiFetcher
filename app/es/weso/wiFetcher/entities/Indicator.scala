package es.weso.wiFetcher.entities

import es.weso.wiFetcher.entities.IndicatorType._
import es.weso.wiFetcher.entities.IndicatorHighLow._
import java.util.Date
import es.weso.wiFetcher.entities.traits.Component

case class Indicator(
  val id: String = null,
  val indicatorType: IndicatorType = null,
  val label: String = "",
  val comment: String = "",
  val intervalStarts: Date = null,
  val interfalFinishes: Date = null,
  val countriesCoverage: Int = 0,
  val weight: Double = 0.0,
  val highLow: IndicatorHighLow = null,
  val source: String = "",
  val component: Component = null,
  val provider:String = "") {

  override def equals(o: Any) = o match {
    case that: Indicator => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }

  override def hashCode(): Int = {
    id.hashCode()
  }

}