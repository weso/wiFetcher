package es.weso.wiFetcher.entities

import scala.collection.mutable.ListBuffer

class Region {
  
  var name = ""
  private val countries : ListBuffer[Country] = new ListBuffer[Country]()
  
  def getCountries() : List[Country] = {
    countries.toList
  }
  
  def addCountry(country : Country) = {
    countries += country
  }
  
  override def equals(o : Any) = o match {
    case that : Region => that.name.equals(name)
  }
  
  override def hashCode() : Int = {
    name.hashCode
  }
}