package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.Matchers
import es.weso.wiFetcher.entities.Region
import java.io.FileInputStream
import es.weso.wiFetcher.utils.FileUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.dao.poi.RegionDAOImpl
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import java.io.File

class RegionDAOImplSteps extends ScalaDsl with EN with Matchers{

  var regions : List[Region] = null
  var numRegions = 0
  var region : Region = null
  
  Given("""I want to load all information about regions$""") {() =>
    val fetcher = SpreadsheetsFetcher(
        new File(FileUtils.getFilePath("files/Structure.xlsx", true)),
        new File(FileUtils.getFilePath("files/example.xlsx", true)))
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val regionsDao : RegionDAO = new RegionDAOImpl(is)(fetcher)
    regions = regionsDao.getRegions
  }
  
  When("""I check the numbers of regions$"""){() =>
    numRegions = regions.size
  }
  
  When("""I check the region with the name "([^"]*)"$"""){(regionName : String) =>
    region = regions.find(region => region.name.equals(regionName)).getOrElse(throw new IllegalArgumentException("There is no region with name " + regionName))
  } 
  
  Then("""the region should have "([^"]*)" countries$"""){(countries : Int) => 
    region.getCountries.size should be (countries)
  }
  
  Then("""the region should have the country "([^"]*)"$"""){(countryName : String) =>
    region.getCountries.find(country => country.name.equals(countryName)).getOrElse(null) should not be (null)
  }
  
  Then("""the region should not have the country "([^"]*)"$"""){(countryName : String) =>
    region.getCountries.find(country => country.name.equals(countryName)).getOrElse(null) should be (null)
  }
  
  Then("""the number of regions should be "([^"]*)"$""") {(result : Int) =>
    numRegions should be (result)
  }
}