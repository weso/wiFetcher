package es.weso.wiFetcher

import es.wes.wiFetcher.fetchers.IndabaFetcher
import es.weso.wiFetcher.dao.CountryDAO
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.dao.CountryDAOImpl
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.IndicatorDAOImpl
import es.weso.wiFetcher.dao.RegionDAOImpl

object Main {

  def main(args: Array[String]): Unit = {
    val regionDao = new RegionDAOImpl("files/Structure.xlsx", true)
    val regions = regionDao.getRegions
    regions.foreach(region => {
      println(region.name + " Countries: ") 
      region.getCountries.foreach(country => print(country.name + " "))
    })
  }

}