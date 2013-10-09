package es.weso.wiFetcher.dao

import java.io.FileNotFoundException
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.FileUtils
import java.io.FileInputStream
import es.weso.wiFetcher.dao.poi.SubIndexDAOImpl

@RunWith(classOf[JUnitRunner])
class SubIndexDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  var subIndexDao : SubIndexDAO = null
  var emptyDao : SubIndexDAO = null
  
  before {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    subIndexDao = new SubIndexDAOImpl(is)(null)
    val is2 = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
        true))
    emptyDao = new SubIndexDAOImpl(is2)(null)
  }

  test("Try to load subindexes data from inexisting file") {
    intercept[FileNotFoundException] {
      val is = new FileInputStream(FileUtils.getFilePath("test.xlsx", true))
      val subindexdao = new SubIndexDAOImpl(is)(null) 
    }
  }
  
  test("Try to load subindexes data from null path") {
    intercept[IllegalArgumentException] {
      val is = new FileInputStream(FileUtils.getFilePath(null, true))
      val subindexdao = new SubIndexDAOImpl(is)(null)
    }
  }
  
  test("Load all subindex data from correct excel file") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", true))
    subIndexDao = new SubIndexDAOImpl(is)(null)
    subIndexDao should not be (null)
    subIndexDao.getSubIndexes.size should not be (0)
    subIndexDao.getComponents.size should not be (0)
  }
  
  test("Obtain all subindexes") {
    subIndexDao.getSubIndexes should not be (null)
    subIndexDao.getSubIndexes.size should be (7)
  }
  
  test("Obtain all components") {
    subIndexDao.getComponents should not be (null)
    subIndexDao.getComponents.size should be (11)
  }
  
  test("Obtain subindexes from empty excel file") {
    emptyDao.getSubIndexes should not be null
    emptyDao.getSubIndexes.size should be (0)
  }
  
  test("Obtain components from empty excel file") {
    emptyDao.getSubIndexes should not be null
    emptyDao.getSubIndexes.size should be (0)
  }
  
  test("Validate components by subindexes") {
    subIndexDao.getSubIndexes.foreach(subindex => {
      subindex.id match {
        case "Readiness" => subindex.getComponents.size should be (2)
        case "TheWeb" => subindex.getComponents.size should be (2)
        case "Impact" => subindex.getComponents.size should be (3)
        case "AB" => subindex.getComponents.size should be (1)
        case "C" => subindex.getComponents.size should be (1)
        case "DQ1" => subindex.getComponents.size should be (1)
        case "Q2" => subindex.getComponents.size should be (1)
      }
    })
  }
  
  test("Validate indicators by components") {
    /*SpreadsheetsFetcher.components.foreach(component => {
      component.id match {
        case "CommunicationsInfrastructure" => component.getIndicators.size should be (13)
        case "InstitutionalInfrastructure" => component.getIndicators.size should be (24)
        case "WebUse" => component.getIndicators.size should be (7)
        case "WebContent" => component.getIndicators.size should be (24)
        case "Social" => component.getIndicators.size should be (5)
        case "Economic" => component.getIndicators.size should be (8)
        case "Political" => component.getIndicators.size should be (4)
        case "ABQ2" => component.getIndicators.size should be (3)
        case "CDQ1" => component.getIndicators.size should be (3)
      }
    })*/
    throw new Exception("Incomplete test")
  }
}