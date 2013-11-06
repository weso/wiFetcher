package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.FileNotFoundException
import es.weso.wiFetcher.entities.IndicatorHighLow
import es.weso.wiFetcher.entities.IndicatorType
import java.io.FileInputStream
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.dao.poi.IndicatorDAOImpl
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import java.io.File
import es.weso.wiFetcher.utils.IssueManagerUtils
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.Entity
import es.weso.wiFetcher.dao.poi.PoiDAO
import es.weso.wiFetcher.entities.Provider
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

@RunWith(classOf[JUnitRunner])
class IndicatorDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers {
  
  implicit val fetcher : SpreadsheetsFetcher = SpreadsheetsFetcher(
      new File(FileUtils.getFilePath("files/Structure.xlsx", true)),
      new File(FileUtils.getFilePath("files/example.xlsx", true)))
  
  test("Try to load indicators information given a null path") {
    intercept[IllegalArgumentException] {
      val is = new FileInputStream(FileUtils.getFilePath(null, true))
      new IndicatorDAOImpl(is)
    }
  }
  
  test("Try to load indicators information from a non-existing file") {
    intercept[FileNotFoundException] {
      val is = new FileInputStream(FileUtils.getFilePath("test.txt", true))
      new IndicatorDAOImpl(is)
    }
  }  
  
  test("Load indicators information from a correct excel file") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val totalSize = indicatorDAO.getPrimaryIndicators.size + indicatorDAO.getSecondaryIndicators.size
    totalSize should be (91)
  }
  
  test("Obtain primary indicators") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (53)
  }
  
  test("Obtain secondary indicators") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (38)
  }
  
  test("Obtain primary indicator from empty excel") {
    val is = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (0)
  }
  
   test("Obtain secondary indicator from empty excel") {
     val is = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
         true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (0)
  }
  
  test("Create a secondary indicator") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val component = new Entity("Q2", HashMap("en" -> "Q2"),
        HashMap("en" -> "Q2 Description"), 0.0) with Component
    val indicator = indicatorDAO.createIndicator("test", "Secondary", 
        HashMap("en" -> "Test indicator"), HashMap("en" -> "Test description"), 
        0.5, "High", "Source", 
        component, ListBuffer(Provider("", "", "", "")))
    indicator.id should be ("test")
    indicator.component.id should be ("Q2")
    indicator.indicatorType should be (IndicatorType.Secondary)
  }
  
  test("Create a primary indicator") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val component = new Entity("Q2", HashMap("en" ->"Q2"), 
        HashMap("en" -> "Q2 Description"), 0.0) with Component
    val indicator = indicatorDAO.createIndicator("test", "Primary", 
        HashMap("en" -> "Test indicator"), HashMap("en" -> "Test description"), 
        0.5, "High", "Source", 
        component, ListBuffer(Provider("", "", "", "")))
    indicator.id should be ("test")
    indicator.component.id should be ("Q2")
    indicator.indicatorType should be (IndicatorType.Primary)
  }
  
  test("Create an indicator given an incorrect type") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val component = new Entity("Q2", HashMap("en" -> "Q2"), 
        HashMap("en" -> "Q2 Description"), 0.0) with Component
    val before = fetcher.issueManager.asSeq.size
    indicatorDAO.createIndicator("test", "AAAA", 
        HashMap("en" -> "Test indicator"), 
      HashMap("en" -> "Test description"), 0.5, "High", "Source", 
      component, ListBuffer(Provider("", "", "", "")))
    val after = fetcher.issueManager.asSeq.size
    after should be (before + 1)
  }
  
  test("Create an indicator given an incorrect HighLow property") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val component = new Entity("Q2", HashMap("en" -> "Q2"), 
        HashMap("en" -> "Q2 Description"), 0.0) with Component
    val before = fetcher.issueManager.asSeq.size
    indicatorDAO.createIndicator("test", "Primary", 
        HashMap("en" -> "Test indicator"), 
	  HashMap("en" -> "Test description"), 0.5, "BBBB", "Source", 
      component, ListBuffer(Provider("", "", "", "")))
    val after = fetcher.issueManager.asSeq.size
    after should be (before + 1)
  }  

}