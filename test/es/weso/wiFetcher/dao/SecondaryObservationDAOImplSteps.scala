package es.weso.wiFetcher.dao

import org.scalatest.Matchers
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Observation
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.io.FileInputStream
import java.io.File
import es.weso.wiFetcher.utils.FileUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.dao.poi.SecondaryObservationDAOImpl
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.StepsUtils

class SecondaryObservationDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  var observationDAO : ObservationDAO = null
  var observations : List[Observation] = null
  var result : Observation = null
  var dataset : Dataset = null
  
  Given("""^I want to load the observations of dataset "([^"]*)"$"""){ (datast:String) =>
    StepsUtils.vars.clear
	val fetcher = SpreadsheetsFetcher(
        new File(FileUtils.getFilePath("files/Structure.xlsx", true)),
        new File(FileUtils.getFilePath("files/example.xlsx", true)))
    dataset = Dataset(datast)
	val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/Raw.xlsx", true)))
	observationDAO = new SecondaryObservationDAOImpl(is)(fetcher)
	observations = observationDAO.getObservations
  } 
  
  When("""^I check the value for the country "([^"]*)" and year "([^"]*)"$""") {(regionName : String, year : Int) =>
    result = observations.find(obs => (obs.area.name.equals(regionName) && obs.year.equals(year) && obs.dataset.equals(dataset))).getOrElse(null)
    StepsUtils.vars.put(StepsUtils.VALUE, result.value.get.toString)
    result should not be null
  }

}