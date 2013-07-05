package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import java.io.FileNotFoundException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegionDAOImplSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{
  
  var regionDao : RegionDAO = null
  var emptyDao : RegionDAO = null
  
  before{
    regionDao = new RegionDAOImpl("files/Structure.xlsx", true)
    emptyDao = new RegionDAOImpl("files/empty.xlsx", true)
  }

  test("Try to load data from a non-existing file") {
    intercept[FileNotFoundException] {
      new RegionDAOImpl("", true)
    }
  }
  
  test("Try to load data from null file") {
    intercept[IllegalArgumentException] {
      new RegionDAOImpl(null, true)
    }
  }
  
  test("Load data correctly") {
    val regionDao = new RegionDAOImpl("files/Structure.xlsx", true)
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