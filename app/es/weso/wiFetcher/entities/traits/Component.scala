package es.weso.wiFetcher.entities.traits

import scala.collection.mutable.HashSet
import org.apache.log4j.Logger
import es.weso.wiFetcher.entities.Entity
import es.weso.wiFetcher.entities.Indicator

trait Component extends Entity {
  private val indicators: HashSet[Indicator] = new HashSet[Indicator]

  var logger = Logger.getLogger(this.getClass())

  def getIndicators(): List[Indicator] = {
    indicators.toList
  }

  def addIndicator(indicator: Indicator) {
    indicators.add(indicator)
  }

  override def equals(o: Any): Boolean = {
    o match {
      case o: Component => o.id.equals(this.id)
      case _ => false
    }
  }

}