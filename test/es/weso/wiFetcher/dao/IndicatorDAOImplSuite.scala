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

@RunWith(classOf[JUnitRunner])
class IndicatorDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
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
    SpreadsheetsFetcher.loadStructure(new File(
        FileUtils.getFilePath("files/Structure0.4.xlsx", true)))
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val totalSize = indicatorDAO.getPrimaryIndicators.size + indicatorDAO.getSecondaryIndicators.size
    totalSize should be (116)
  }
  
  test("Obtain primary indicators") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (72)
  }
  
  test("Obtain secondary indicators") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (44)
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
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicator = indicatorDAO.createIndicator("test", "Secondary", 
        "Test indicator", "Test description", "0.5", "High", "Source", 
        "affordability", "provider")
    indicator.id should be ("test")
    indicator.component.id should be ("affordability")
    indicator.indicatorType should be (IndicatorType.Secondary)
  }
  
  test("Create a primary indicator") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    val indicator = indicatorDAO.createIndicator("test", "Primary", 
        "Test indicator", "Test description", "0.5", "High", "Source", 
        "infrastructure", "provider")
    indicator.id should be ("test")
    indicator.component.id should be ("infrastructure")
    indicator.indicatorType should be (IndicatorType.Primary)
  }
  
  test("Create an indicator given an incorrect type") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    intercept[IllegalArgumentException]{
      indicatorDAO.createIndicator("test", "AAAA", "Test indicator", 
        "Test description", "0.5", "High", "Source", 
        "component", "provider")
    }
  }
  
  test("Create an indicator given an incorrect HighLow property") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure0.4.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    intercept[IllegalArgumentException]{
      indicatorDAO.createIndicator("test", "Primary", "Test indicator", 
        "Test description", "0.5", "BBBB", "Source", 
        "component", "provider")
    }
  }  

}