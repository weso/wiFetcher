package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.entities.Observation
import java.io.FileNotFoundException
import es.weso.wiFetcher.entities.Dataset
import java.io.FileInputStream
import java.io.File
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.dao.poi.ObservationDAOImpl

@RunWith(classOf[JUnitRunner])
class ObservationDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{

  var observationDAO : ObservationDAO = null
  var dataset1 : Dataset = null
  var dataset2 : Dataset = null
  
  before {
    dataset1 = new Dataset
    dataset2 = new Dataset
    dataset1.id = "2008"
    dataset2.id = "2009"
  }
  
  test("Try to load a non-existing spreadsheet") {
    intercept[FileNotFoundException]{
      val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/text.xlsx", true)))
      observationDAO  = new ObservationDAOImpl(is)
    }
  }
  
  test("Load a correct and an existing spreadsheet") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO = new ObservationDAOImpl(is)
  }
  
  test("Try to obtain all observations of a list of datasets null") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO = new ObservationDAOImpl(is)
    intercept[IllegalArgumentException] {
      observationDAO.getObservations(null)
    }
  }
  
  test("Try to obtain all observations of a empty list of datasets") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO  = new ObservationDAOImpl(is)
    var datasets : List[Dataset] = List[Dataset]()
    var observations : List[Observation] = observationDAO.getObservations(datasets)
    observations should not be null
    observations.size should be (0)
  }
  
  test("Obatin all observations of a list of datasets") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO  = new ObservationDAOImpl(is)
    var datasets : List[Dataset] = List[Dataset](dataset1, dataset2)
    var observations : List[Observation] = observationDAO.getObservations(datasets)
    observations should not be null
    observations.size should be (6069)
  }
  
  test("Try to obtain all observations of a non-existing dataset") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO = new ObservationDAOImpl(is)
    var dataset : Dataset = new Dataset
    dataset.id = "test"
    intercept[IllegalArgumentException] {
      observationDAO.getObservationsByDataset(dataset)
    }  
  }
  
  test("Try to obtain all observations of a null dataset") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO = new ObservationDAOImpl(is)
    intercept[IllegalArgumentException] {
      observationDAO.getObservationsByDataset(null)
    }
  }
  
  test("Obtain all observations of a valid dataset") {
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    observationDAO  = new ObservationDAOImpl(is)
    var observations : List[Observation] = observationDAO.getObservationsByDataset(dataset1)
    observations should not be null
    observations.size should be (3645)
  }
  
}