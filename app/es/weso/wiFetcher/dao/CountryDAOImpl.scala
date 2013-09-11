package es.weso.wiFetcher.dao

import scala.collection.immutable.List
import es.weso.wiFetcher.entities.Country
import scala.io.Source
import es.weso.wiFetcher.configuration.Configuration
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.utils.FileUtils
import java.io.FileNotFoundException

/**
 * This class contains the implementation that allows to load all information 
 * about countries that are used in the Web Index.
 * 
 * At the moment, Web Foundation does not define the structure of files and 
 * temporally, we use a TSV file that contains the minimum information about 
 * countries. Their name, iso-2 code and iso-3 code.  
 */
class CountryDAOImpl(path : String, relativePath : Boolean) extends CountryDAO {
  println("Carga fichero paises")
  private var countries : List[Country] = load(FileUtils.getFilePath(path, 
      relativePath))
  
  /**
   * This method load all information about countries and stored them in a list
   * @param path Path of the file that contains the information about countries
   * @return A list with all countries loaded from the file
   */
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

  /**
   * This method returns a list of the countries
   */
  def getCountries() : List[Country] = {
    countries
  }
}