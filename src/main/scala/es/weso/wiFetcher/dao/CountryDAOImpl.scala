package es.weso.wiFetcher.dao

import scala.collection.immutable.List
import es.weso.wiFetcher.entities.Country
import scala.io.Source
import es.weso.wiFetcher.configuration.Configuration
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.utils.FileUtils
import java.io.FileNotFoundException

class CountryDAOImpl(path : String, relativePath : Boolean) extends CountryDAO {
  
  private var countries : List[Country] = load(FileUtils.getFilePath(path, 
      relativePath))
  
  private def load(path : String): List[Country] = {
    var countries : ListBuffer[Country] = new ListBuffer[Country]()
    val src = Source.fromFile(path)
    val iter = src.getLines().map(_.split("\t"))
    iter.foreach(line => {
      var country : Country = new Country
      country.name = line(0)
      country.iso2Code = line(1)
      country.iso3Code = line(2)
      countries += (country)
    })
    countries.toList
  }

  def getCountries() : List[Country] = {
    countries
  }
}