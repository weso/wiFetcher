package es.weso.wiFetcher.entities

import scala.collection.mutable.ListBuffer

case class Region(val name:String) {

  private val countries: ListBuffer[Country] = new ListBuffer[Country]()

  def getCountries(): List[Country] = {
    countries.toList
  }

  def addCountry(country: Country) = {
    countries += country
  }

  override def equals(o: Any) = o match {
    case that: Region => that.name.equals(name)
  }

  override def hashCode(): Int = {
    name.hashCode
  }
}