package es.weso.wiFetcher.entities

import es.weso.wiFetcher.entities.IndicatorType._
import es.weso.wiFetcher.entities.IndicatorHighLow._
import java.util.Date

class Indicator {
  
  var id : String = null
  var indicatorType : IndicatorType = null
  var label : String = "" 
  var comment : String = ""
  var intervalStarts : Date = null  
  var intervalFinishes : Date = null 
  var countriesCoverage : Int = 0
  var weight : Double = 0.0
  var highLow : IndicatorHighLow = null
  var source : String = "";
  var component : Component = null
  
  override def equals(o : Any) = o match {
    case that : Indicator => that.id.equalsIgnoreCase(this.id)
    case _ => false
  }
  
  override def hashCode() : Int = {
    id.hashCode()
  }
  

}