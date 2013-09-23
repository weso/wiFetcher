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
    var totalSize = indicatorDAO.getSecondaryIndicators.size
    totalSize should be (38)
    indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "Secondary", "0.5", 
        "High")
    totalSize = indicatorDAO.getSecondaryIndicators.size
    totalSize should be (39)
  }
  
  test("Create a primary indicator") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    var totalSize = indicatorDAO.getPrimaryIndicators.size
    totalSize should be (53)
    indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "Primary", "0.5", 
        "High")
    totalSize = indicatorDAO.getPrimaryIndicators.size
    totalSize should be (54)
  }
  
  test("Create an indicator given an incorrect type") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    intercept[IllegalArgumentException]{
      indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "", "0.5", 
        "High")
    }
  }
  
  test("Create an indicator given an incorrect HighLow property") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val indicatorDAO = new IndicatorDAOImpl(is)
    intercept[IllegalArgumentException]{
      indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "Primary", "0.5", 
        "")
    }
  }  

}