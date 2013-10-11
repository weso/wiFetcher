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
import es.weso.wiFetcher.dao.poi.SecondaryObservationDAOImpl
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import org.scalatest.BeforeAndAfterAll

@RunWith(classOf[JUnitRunner])
class SecondaryObservationDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers with BeforeAndAfterAll{
  
  test("Try to load a non-existing spreadsheet") {
    intercept[FileNotFoundException]{
      val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/text.xlsx", true)))
      val observationDAO  = new SecondaryObservationDAOImpl(is)(null)
    }
  }
  
  test("Load a correct and an existing spreadsheet") {
    val fetcher = SpreadsheetsFetcher(
        new File(FileUtils.getFilePath("files/structure.xlsx", true)),
        new File(FileUtils.getFilePath("files/example.xlsx", true)))
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/example.xlsx", true)))
    val observationDAO = new SecondaryObservationDAOImpl(is)(fetcher)
    observationDAO.getObservations.size should be (120)
  }
  
}