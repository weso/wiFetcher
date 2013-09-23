package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers
import org.scalatest.FunSuite
import java.io.FileNotFoundException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.FileInputStream
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.dao.poi.RegionDAOImpl

@RunWith(classOf[JUnitRunner])
class RegionDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  var regionDao : RegionDAO = null
  var emptyDao : RegionDAO = null
  
  before{
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    regionDao = new RegionDAOImpl(is)
    val is2 = new FileInputStream(FileUtils.getFilePath("files/empty.xlsx", 
        true))
    emptyDao = new RegionDAOImpl(is2)
  }

  test("Try to load data from a non-existing file") {
    intercept[FileNotFoundException] {
      val is = new FileInputStream(FileUtils.getFilePath("", true))
      new RegionDAOImpl(is)
    }
  }
  
  test("Try to load data from null file") {
    intercept[IllegalArgumentException] {
      val is = new FileInputStream(FileUtils.getFilePath(null, true))
      new RegionDAOImpl(is)
    }
  }
  
  test("Load data correctly") {
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val regionDao = new RegionDAOImpl(is)
    regionDao should not be null
    regionDao.getRegions.size should not be(0)
  }
  
  test("Obtain all regions") {
    val regions = regionDao.getRegions
    regions should not be null
    regions.size should be (5)
  }
  
  test("Obtain all regions from empty file") {
    val regions = emptyDao.getRegions
    regions should not be null
    regions.size should be (0)
  }
  
  test("Validate countries for regions") {
    val regions = regionDao.getRegions
    regions.foreach(region => {
      region.name match {
        case "Africa" => region.getCountries.size should be (18)
        case "Americas" => region.getCountries.size should be (9)
        case "Asia pacific" => region.getCountries.size should be (14)
        case "Europe" => region.getCountries.size should be (15)
        case "Middle east & Central asia" => region.getCountries.size should be (5)
      } 
    })
  }
  
}