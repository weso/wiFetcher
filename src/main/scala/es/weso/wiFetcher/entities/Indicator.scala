package es.weso.wiFetcher.entities

import es.weso.wiFetcher.entities.IndicatorType._
import java.util.Date

class Indicator (var indicatorType : IndicatorType, var label : String, 
    var comment : String, var intervalStarts : Date, 
    var interfalFinishes : Date, var countriesCoverage : Int){
  

}