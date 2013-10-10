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

@RunWith(classOf[JUnitRunner])
class IndicatorDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  val fetcher : SpreadsheetsFetcher = SpreadsheetsFetcher(
      new File(FileUtils.getFilePath("files/structure.xlsx", true)),
      new File(FileUtils.getFilePath("files/example.xlsx", true)))
  
  test("Try to load indicators information given a null path") {
    intercept[IllegalArgumentException] {
      val is = new FileInputStream(FileUtils.getFilePath(null, true))
      new IndicatorDAOImpl(is)(fetcher)
    }
  }
  
  test("Try to load indicators information from a non-existing file") {
    intercept[FileNotFoundException] {
      val is = new FileInputStream(FileUtils.getFilePath("test.txt", true))
      new IndicatorDAOImpl(is)(fetcher)
    }
  }  
  
  test("Load indicators information from a correct excel file") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val totalSize = indicatorDAO.getPrimaryIndicators.size + indicatorDAO.getSecondaryIndicators.size
    totalSize should be (6)
  }
  
  test("Obtain primary indicators") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (2)
  }
  
  test("Obtain secondary indicators") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (4)
  }
  
  test("Obtain primary indicator from empty excel") {
    val is = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (0)
  }
  
   test("Obtain secondary indicator from empty excel") {
     val is = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
         true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (0)
  }
  
  test("Create a secondary indicator") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val indicator = indicatorDAO.createIndicator("test", "Secondary", 
        "Test indicator", "Test description", "0.5", "High", "Source", 
        "Q2", "provider")
    indicator.id should be ("test")
    indicator.component.id should be ("Q2")
    indicator.indicatorType should be (IndicatorType.Secondary)
  }
  
  test("Create a primary indicator") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val indicator = indicatorDAO.createIndicator("test", "Primary", 
        "Test indicator", "Test description", "0.5", "High", "Source", 
        "Q2", "provider")
    indicator.id should be ("test")
    indicator.component.id should be ("Q2")
    indicator.indicatorType should be (IndicatorType.Primary)
  }
  
  test("Create an indicator given an incorrect type") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val before = fetcher.issueManager.asSeq.size
    indicatorDAO.createIndicator("test", "AAAA", "Test indicator", 
      "Test description", "0.5", "High", "Source", 
      "Q2", "provider")
    val after = fetcher.issueManager.asSeq.size
    after should be (before + 1)
  }
  
  test("Create an indicator given an incorrect HighLow property") {
    val is = new FileInputStream(FileUtils.getFilePath("files/structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)(fetcher)
    val before = fetcher.issueManager.asSeq.size
    indicatorDAO.createIndicator("test", "Primary", "Test indicator", 
	  "Test description", "0.5", "BBBB", "Source", 
      "Q2", "provider")
    val after = fetcher.issueManager.asSeq.size
    after should be (before + 1)
  }  

}