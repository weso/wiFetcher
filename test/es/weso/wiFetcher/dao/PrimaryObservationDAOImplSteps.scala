package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.Matchers
import es.weso.wiFetcher.entities.Observation
import java.io.FileInputStream
import java.io.File
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.dao.poi.SecondaryObservationDAOImpl
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

class PrimaryObservationDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  var observationDAO : ObservationDAO = null
  var observations : List[Observation] = null
  var result : Observation = null
  
  Given("""^I want to load the observations of primary indicators$""") {() => 
     val fetcher = SpreadsheetsFetcher(
        new File(FileUtils.getFilePath("files/Structure.xlsx", true)),
        new File(FileUtils.getFilePath("files/example.xlsx", true)))
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/example.xlsx", true)))
	observationDAO = new SecondaryObservationDAOImpl(is)(fetcher)
  }
  
  When("""^I check the value for the country "([^"]*)" and indicator "([^"]*)"$""") {(regionName : String, indicator : String) =>
    result = observations.find(obs => (obs.area.name.equals(regionName) && obs.indicator != null && obs.indicator.id.equals(indicator))).getOrElse(null)
    result should not be null
  }
  
  Then("""the value should be "([^"]*)"$""") { (value : Double) =>
    value should be(value +- 0.0000001f)
  }
  
  Then("""the value should not be "([^"]*)"$""") { (value : Double) =>
    value should not be(value +- 0.0000001f)
  }

}