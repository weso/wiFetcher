package es.weso.wiFetcher

import es.wes.wiFetcher.fetchers.IndabaFetcher
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.entities.Dataset

object Main {

  def main(args: Array[String]): Unit = {
    /*val f = new IndabaFetcher
    f.fetch("demo.csv")*/
    val f = new SpreadsheetsFetcher
    f.loadWorkbook("files/rawdata.xlsx", true)
    var dataset : Dataset = new Dataset
    dataset.id = "ITUA"
    f.extractObservationsByDataset(dataset)
  }

}