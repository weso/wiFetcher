package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.Matchers
import es.weso.wiFetcher.entities.Observation
import java.io.FileInputStream
import java.io.File
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.StepsUtils
import es.weso.wiFetcher.dao.poi.PrimaryObservationDAOImpl

class PrimaryObservationDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  var observationDAO : ObservationDAO = null
  var observations : List[Observation] = null
  var result : Observation = null
  
  Given("""^I want to load the observations of primary indicators$""") {() =>
    StepsUtils.vars.clear
     val fetcher = SpreadsheetsFetcher(
        new File(FileUtils.getFilePath("files/structureSimple0.2.xlsx", true)),
        new File(FileUtils.getFilePath("files/example.xlsx", true)))
    val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/example.xlsx", true)))
	observationDAO = new PrimaryObservationDAOImpl(is)(fetcher)
	observations = observationDAO.getObservations
  }
  
  When("""^I check the value for the country "([^"]*)" and indicator "([^"]*)"$""") {(regionName : String, indicator : String) =>
    result = observations.find(obs => (obs.area.name.equals(regionName) && obs.indicator != null && obs.indicator.id.equals(indicator))).getOrElse(null)
    StepsUtils.vars.put(StepsUtils.VALUE, result.value.get.toString)
    result should not be null
  }

}