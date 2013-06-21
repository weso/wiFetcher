package es.weso.wiFetcher

import es.wes.wiFetcher.fetchers.IndabaFetcher
import es.weso.wiFetcher.dao.CountryDAO
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.dao.CountryDAOImpl
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.IndicatorDAOImpl

object Main {

  def main(args: Array[String]): Unit = {
    /*val f = new IndabaFetcher
    f.fetch("demo.csv")*/
//    val f = new SpreadsheetsFetcher
//    f.loadWorkbook("files/rawdata.xlsx", true)
//    var dataset : Dataset = new Dataset
//    dataset.id = "ITUA"
//    f.extractObservationsByDataset(dataset)
//    var dao = new CountryDAOImpl(Configuration.getCountryFile, true)
//    println(dao.getCountries)
//    var dao = new IndicatorDAOImpl("Structure.xlsx", true)
  }

}