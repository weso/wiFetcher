package es.weso.wiFetcher.entities

class Country extends Area{
	var defName : String = "foo"
	var names : Map[String, String] = Map.empty
	var lat : Double = 0
	var long : Double = 0
}