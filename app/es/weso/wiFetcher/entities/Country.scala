package es.weso.wiFetcher.entities

case class Country(
    val lat:Option[Double]=None, 
    val lon:Option[Double]=None, 
    name:String, 
    iso2Code:String, 
    iso3Code:String
) extends Area