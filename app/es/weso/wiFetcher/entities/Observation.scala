package es.weso.wiFetcher.entities

import es.weso.wiFetcher.entities.ObservationStatus._

class Observation {
  
    var dataset : Dataset = null
    var label : String = "" 
    var area : Area = null 
    var computation : Computation = null 
    var indicator : Indicator = null 
    var year : Int = -1 
    var value : Double = -1 
    var status : ObservationStatus = null

}