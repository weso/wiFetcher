package es.weso.wiFetcher.entities

import es.weso.wiFetcher.entities.ObservationStatus._

case class Observation ( 
    val dataset : Dataset = null,
    val label : String = "",
    val area : Area = null,
    val computation : Computation = null,
    val indicator : Indicator = null,
    val year : Int = -1,
    val value : Option[Double] = None, 
    val status : ObservationStatus = null,
    val sheet : ObservationStatus = null
)