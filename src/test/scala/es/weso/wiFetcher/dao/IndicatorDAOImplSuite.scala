package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.FileNotFoundException
import es.weso.wiFetcher.entities.IndicatorHighLow
import es.weso.wiFetcher.entities.IndicatorType

@RunWith(classOf[JUnitRunner])
class IndicatorDAOImplSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{
  
  test("Try to load indicators information given a null path") {
    intercept[IllegalArgumentException] {
      new IndicatorDAOImpl(null, true)
    }
  }
  
  test("Try to load indicators information from a non-existing file") {
    intercept[FileNotFoundException] {
      new IndicatorDAOImpl("test.txt", true)
    }
  }  
  
  test("Load indicators information from a correct excel file") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    val totalSize = indicatorDAO.getPrimaryIndicators.size + indicatorDAO.getSecondaryIndicators.size
    totalSize should be (91)
  }
  
  test("Obtain primary indicators") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (53)
  }
  
  test("Obtain secondary indicators") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (38)
  }
  
  test("Obtain primary indicator from empty excel") {
    val indicatorDAO = new IndicatorDAOImpl("files/empty.xlsx", true)
    val indicators = indicatorDAO.getPrimaryIndicators
    indicators should not be (null)
    indicators.size should be (0)
  }
  
   test("Obtain secondary indicator from empty excel") {
    val indicatorDAO = new IndicatorDAOImpl("files/empty.xlsx", true)
    val indicators = indicatorDAO.getSecondaryIndicators
    indicators should not be (null)
    indicators.size should be (0)
  }
  
  test("Create a secondary indicator") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    var totalSize = indicatorDAO.getSecondaryIndicators.size
    totalSize should be (38)
    indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "Secondary", "0.5", 
        "High")
    totalSize = indicatorDAO.getSecondaryIndicators.size
    totalSize should be (39)
  }
  
  test("Create a primary indicator") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    var totalSize = indicatorDAO.getPrimaryIndicators.size
    totalSize should be (53)
    indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "Primary", "0.5", 
        "High")
    totalSize = indicatorDAO.getPrimaryIndicators.size
    totalSize should be (54)
  }
  
  test("Create an indicator given an incorrect type") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    intercept[IllegalArgumentException]{
      indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "", "0.5", 
        "High")
    }
  }
  
  test("Create an indicator given an incorrect HighLow property") {
    val indicatorDAO = new IndicatorDAOImpl("files/Structure.xlsx", true)
    intercept[IllegalArgumentException]{
      indicatorDAO.createIndicator("test", "TheWeb", "WebContent", "test", 
        "test description", "WESO", "WESO", "Primary", "0.5", 
        "")
    }
  }  

}