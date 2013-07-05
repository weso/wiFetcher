package es.weso.wiFetcher.entities

import scala.collection.mutable.HashSet
import org.apache.log4j.Logger

class Component {
  
  var id : String = "";
  var name : String = ""
  var description : String = ""
  var weight : Double = 0.0 
  private var indicators : HashSet[Indicator] = new HashSet[Indicator]
  
  var logger = Logger.getLogger(this.getClass())
  
  def getIndicators() : List[Indicator] = {
    indicators.toList
  }
  
  def addIndicator(indicator : Indicator) {
    indicators.add(indicator)
  }

}