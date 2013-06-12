package es.weso.wiFetcher.fetchers

import java.io.FileNotFoundException
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import es.weso.wiFetcher.entities.Dataset
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.entities.Observation


@RunWith(classOf[JUnitRunner])
class SpreadsheetsFetcherSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{
  
  var fetcher : SpreadsheetsFetcher = null
  var dataset1 : Dataset = null
  var dataset2 : Dataset = null
  
  before {
    fetcher = new SpreadsheetsFetcher
    dataset1 = new Dataset
    dataset2 = new Dataset
    dataset1.id = "ITUA-Raw"
    dataset2.id = "ITUA-IMPUTED"
  }
  
  test("Try to load a non-existing spreadsheet") {
    intercept[FileNotFoundException]{
      fetcher.loadWorkbook("files/text.xlsx")
    }
  }
  
  test("Load a correct and an existing spreadsheet") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
  }
  
  test("Try to obtain all observations of a list of datasets null") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
    intercept[IllegalArgumentException] {
      fetcher.getObservations(null)
    }
  }
  
  test("Try to obtain all observations of a empty list of datasets") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
    var datasets : List[Dataset] = List[Dataset]()
    var observations : List[Observation] = fetcher.getObservations(datasets)
    observations should not be null
    observations.size should be (0)
  }
  
  test("Obatin all observations of a list of datasets") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
    var datasets : List[Dataset] = List[Dataset](dataset1, dataset2)
    var observations : List[Observation] = fetcher.getObservations(datasets)
    observations should not be null
    observations.size should be (60)
  }
  
  test("Try to obtain all observations of a non-existing dataset") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
    var dataset : Dataset = new Dataset
    dataset.id = "test"
    intercept[IllegalArgumentException] {
      fetcher.extractObservationsByDataset(dataset)
    }  
  }
  
  test("Try to obtain all observations of a null dataset") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
    intercept[IllegalArgumentException] {
      fetcher.extractObservationsByDataset(null)
    }
  }
  
  test("Obtain all observations of a valid dataset") {
    fetcher.loadWorkbook("files/rawdata.xlsx", true)
    var observations : List[Observation] = fetcher.extractObservationsByDataset(dataset1)
    observations should not be null
    observations.size should be (30)
  }

}