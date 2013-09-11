package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.matchers.ShouldMatchers
import es.weso.wiFetcher.entities.Region
import java.io.FileInputStream
import es.weso.wiFetcher.utils.FileUtils

class RegionDAOImplSteps extends ScalaDsl with EN with ShouldMatchers{

  var regions : List[Region] = null
  var numRegions = 0
  var region : Region = null
  
  Given("""I want to load all information about regions$""") {() =>
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    val regionsDao : RegionDAO = new RegionDAOImpl(is)
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