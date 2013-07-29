package es.weso.wiFetcher

import es.wes.wiFetcher.fetchers.IndabaFetcher
import es.weso.wiFetcher.dao.CountryDAO
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.dao.CountryDAOImpl
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.IndicatorDAOImpl
import es.weso.wiFetcher.dao.RegionDAOImpl
import es.weso.wiFetcher.analyzer.indicator.IndicatorReconciliator
import es.weso.wiFetcher.dao.ObservationDAOImpl

object Main {

  def main(args: Array[String]): Unit = {
    val observations = SpreadsheetsFetcher.observations   
    observations.foreach(observation => {
      println("Area: " + observation.area.name + ", Dataset: " + observation.dataset.id
          + ", Indicator: " + observation.indicator.label + ", Year: " + 
          observation.year + ", Value: " + observation.value)
    })
  }

}