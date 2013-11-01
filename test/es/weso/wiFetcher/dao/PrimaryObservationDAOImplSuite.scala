package es.weso.wiFetcher.dao

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.FileNotFoundException
import java.io.FileInputStream
import es.weso.wiFetcher.dao.poi.PrimaryObservationDAOImpl
import java.io.File
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.dao.poi.SecondaryObservationDAOImpl

@RunWith(classOf[JUnitRunner])
class PrimaryObservationDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers with BeforeAndAfterAll{
  
  test("Try to load a non-existing spreadsheet") {
    intercept[FileNotFoundException]{
      val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/text.xlsx", true)))
      val observationDAO  = new PrimaryObservationDAOImpl(is)(null)
    }
  }
  
  test("Load a correct and an existing spreadsheet") {
    val fetcher = SpreadsheetsFetcher(
        new File(FileUtils.getFilePath("files/Structure.xlsx", true)),
        new File(FileUtils.getFilePath("files/example.xlsx", true)))
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/example.xlsx", true)))
    val observationDAO = new PrimaryObservationDAOImpl(is)(fetcher)
    observationDAO.getObservations.size should be (8)
  }

}