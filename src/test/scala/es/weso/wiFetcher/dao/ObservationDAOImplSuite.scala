package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.entities.Observation
import java.io.FileNotFoundException
import es.weso.wiFetcher.entities.Dataset

@RunWith(classOf[JUnitRunner])
class ObservationDAOImplSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{

  var observationDAO : ObservationDAO = null
  var dataset1 : Dataset = null
  var dataset2 : Dataset = null
  
  before {
    dataset1 = new Dataset
    dataset2 = new Dataset
    dataset1.id = "ITUA-Raw"
    dataset2.id = "ITUA-IMPUTED"
  }
  
  test("Try to load a non-existing spreadsheet") {
    intercept[FileNotFoundException]{
      observationDAO  = new ObservationDAOImpl("files/text.xlsx", true)
    }
  }
  
  test("Load a correct and an existing spreadsheet") {
    observationDAO = new ObservationDAOImpl("files/rawdata.xlsx", true)
  }
  
  test("Try to obtain all observations of a list of datasets null") {
    observationDAO = new ObservationDAOImpl("files/rawdata.xlsx", true)
    intercept[IllegalArgumentException] {
      observationDAO.getObservations(null)
    }
  }
  
  test("Try to obtain all observations of a empty list of datasets") {
    observationDAO  = new ObservationDAOImpl("files/rawdata.xlsx", true)
    var datasets : List[Dataset] = List[Dataset]()
    var observations : List[Observation] = observationDAO.getObservations(datasets)
    observations should not be null
    observations.size should be (0)
  }
  
  test("Obatin all observations of a list of datasets") {
    observationDAO  = new ObservationDAOImpl("files/rawdata.xlsx", true)
    var datasets : List[Dataset] = List[Dataset](dataset1, dataset2)
    var observations : List[Observation] = observationDAO.getObservations(datasets)
    observations should not be null
    observations.size should be (60)
  }
  
  test("Try to obtain all observations of a non-existing dataset") {
    observationDAO = new ObservationDAOImpl("files/rawdata.xlsx", true)
    var dataset : Dataset = new Dataset
    dataset.id = "test"
    intercept[IllegalArgumentException] {
      observationDAO.getObservationsByIndicator(dataset)
    }  
  }
  
  test("Try to obtain all observations of a null dataset") {
    observationDAO = new ObservationDAOImpl("files/rawdata.xlsx", true)
    intercept[IllegalArgumentException] {
      observationDAO.getObservationsByIndicator(null)
    }
  }
  
  test("Obtain all observations of a valid dataset") {
    observationDAO  = new ObservationDAOImpl("files/rawdata.xlsx", true)
    var observations : List[Observation] = observationDAO.getObservationsByIndicator(dataset1)
    observations should not be null
    observations.size should be (30)
  }
  
}