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
import java.io.File

@RunWith(classOf[JUnitRunner])
class SubIndexDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  var subIndexDao : SubIndexDAO = null
  var emptyDao : SubIndexDAO = null
  val fetcher : SpreadsheetsFetcher = SpreadsheetsFetcher(
      new File(FileUtils.getFilePath("files/Structure.xlsx", true)),
      new File(FileUtils.getFilePath("files/example.xlsx", true)))
  
  before {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    subIndexDao = new SubIndexDAOImpl(is)(fetcher)
    val is2 = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
        true))
    emptyDao = new SubIndexDAOImpl(is2)(fetcher)
  }

  test("Try to load subindexes data from inexisting file") {
    intercept[FileNotFoundException] {
      val is = new FileInputStream(FileUtils.getFilePath("test.xlsx", true))
      val subindexdao = new SubIndexDAOImpl(is)(fetcher) 
    }
  }
  
  test("Try to load subindexes data from null path") {
    intercept[IllegalArgumentException] {
      val is = new FileInputStream(FileUtils.getFilePath(null, true))
      val subindexdao = new SubIndexDAOImpl(is)(fetcher)
    }
  }
  
  test("Load all subindex data from correct excel file") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", true))
    subIndexDao = new SubIndexDAOImpl(is)(fetcher)
    subIndexDao should not be (null)
    subIndexDao.getSubIndexes.size should not be (0)
    subIndexDao.getComponents.size should not be (0)
  }
  
  test("Obtain all subindexes") {
    subIndexDao.getSubIndexes should not be (null)
    subIndexDao.getSubIndexes.size should be (4)
  }
  
  test("Obtain all components") {
    subIndexDao.getComponents should not be (null)
    subIndexDao.getComponents.size should be (10)
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
        case "access" => subindex.getComponents.size should be (3)
        case "freeopen" => subindex.getComponents.size should be (2)
        case "content" => subindex.getComponents.size should be (2)
        case "empowerment" => subindex.getComponents.size should be (3)
      }
    })
  }
  
  test("Validate indicators by components") {
    fetcher.components.foreach(component => {
      component.id match {
        case "infrastructure" => component.getIndicators.size should be (9)
        case "affordability" => component.getIndicators.size should be (10)
        case "education" => component.getIndicators.size should be (9)
        case "freedom" => component.getIndicators.size should be (8)
        case "openness" => component.getIndicators.size should be (4)
        case "creation" => component.getIndicators.size should be (16)
        case "use" => component.getIndicators.size should be (5)
        case "economic" => component.getIndicators.size should be (11)
        case "political" => component.getIndicators.size should be (13)
        case "social" => component.getIndicators.size should be (6)
      }
    })
  }
}