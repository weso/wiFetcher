package es.weso.wiFetcher.dao.file

import scala.collection.immutable.List
import scala.io.Source
import es.weso.wiFetcher.dao.CountryDAO
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

/**
 * This class contains the implementation that allows to load all information
 * about countries that are used in the Web Index.
 *
 * At the moment, Web Foundation does not define the structure of files and
 * temporally, we use a TSV file that contains the minimum information about
 * countries. Their name, iso-2 code and iso-3 code.
 */
class CountryDAOImpl(path: String, relativePath: Boolean)(implicit val sFetcher: SpreadsheetsFetcher)
  extends CountryDAO with FileDAO[Country] {

  private val countries: List[Country] = load(FileUtils.getFilePath(path, relativePath))

  /**
   * This method load all information about countries and stored them in a list
   * @param path Path of the file that contains the information about countries
   * @return A list with all countries loaded from the file
   */
  protected def load(path: String) : List[Country]={
    parseData(path).toList
  }

  protected def parseData(path: String): Seq[Country] = {
    val src = Source.fromFile(path)
    for {
      line <- src.getLines.toList
      chunks = line.split("\t")
      name = chunks(0)
      iso2Code = chunks(1)
      iso3Code = chunks(2)
    } yield {
      Country(None, None, name, iso2Code, iso3Code)
    }
  }

  /**
   * This method returns a list of the countries
   */
  def getCountries(): List[Country] = {
    countries.toList
  }
}