package es.weso.wiFetcher.entities

class Observation (var dataset : Dataset, var label : String, var area : Area, 
    var computation : Computation, var indicator : Indicator, var year : Int, 
    var value : Double, var status: String){

}