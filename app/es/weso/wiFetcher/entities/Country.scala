package es.weso.wiFetcher.entities

case class Country(
    val lat:Option[Double]=None, 
    val lon:Option[Double]=None, 
    _name:String, 
    _iso2Code:String, 
    _iso3Code:String
) extends Area (_name, _iso2Code, _iso3Code)