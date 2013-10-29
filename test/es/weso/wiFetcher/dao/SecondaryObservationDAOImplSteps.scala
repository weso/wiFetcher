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

class SecondaryObservationDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  var observationDAO : ObservationDAO = null
  var observations : List[Observation] = null
  var result : Observation = null
  
  Given("""^I want to load the observations of dataset "([^"]*)" in the year "([^"]*)"$"""){ (datast:String, year:Int) =>
	val dataset : Dataset = Dataset(datast)
	val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
	observationDAO = new SecondaryObservationDAOImpl(is)(null)
  } 
  
  Given("""^I want to load the observations of non-existing dataset "([^"]*)" in the year "([^"]*)"$""") {(datast : String, year:Int) => {
    val dataset : Dataset = Dataset(datast)
	val is = new FileInputStream(new File(
	    FileUtils.getFilePath("files/TASTemplate.xlsx", true)))
    intercept[IllegalArgumentException] {
	    observationDAO = new SecondaryObservationDAOImpl(is)(null)
    }
  }}
  
  When("""^I check the value for the country "([^"]*)" and indicator "([^"]*)"$""") {(regionName : String, indicator : String) =>
    result = observations.find(obs => (obs.area.name.equals(regionName) && obs.indicator != null && obs.indicator.id.equals(indicator))).getOrElse(null)
    result should not be null
  }
  
  Then("""the value should be "([^"]*)"$""") { (value : Double) =>
    value should be(value +- 0.0000001f)
  }
  
  Then("""it should raise an Exception$""") { () =>
    throw new NotImplementedException
  }

}