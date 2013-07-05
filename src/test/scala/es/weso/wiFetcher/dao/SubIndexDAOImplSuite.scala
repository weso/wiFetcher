package es.weso.wiFetcher.dao

import java.io.FileNotFoundException
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SubIndexDAOImplSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{
  
  var subIndexDao : SubIndexDAO = null
  var emptyDao : SubIndexDAO = null
  
  before {
    subIndexDao = new SubIndexDAOImpl("files/Structure.xlsx", true)
    emptyDao = new SubIndexDAOImpl("files/empty.xlsx", true)
  }

  test("Try to load subindexes data from inexisting file") {
    intercept[FileNotFoundException] {
      val subindexdao = new SubIndexDAOImpl("test.xlsx", true) 
    }
  }
  
  test("Try to load subindexes data from null path") {
    intercept[IllegalArgumentException] {
      val subindexdao = new SubIndexDAOImpl(null, true)
    }
  }
  
  test("Load all subindex data from correct excel file") {
    subIndexDao = new SubIndexDAOImpl("files/Structure.xlsx", true)
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
    subIndexDao.getComponents.foreach(component => {
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
    })
  }
}