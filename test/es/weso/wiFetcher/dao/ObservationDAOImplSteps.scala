package es.weso.wiFetcher.dao

import org.scalatest.matchers.ShouldMatchers
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Observation
import sun.reflect.generics.reflectiveObjects.NotImplementedException

class ObservationDAOImplSteps extends ScalaDsl with EN with ShouldMatchers{
  
  var observationDAO : ObservationDAO = null
  var observations : List[Observation] = null
  var result : Observation = null
  
  Given("""^I want to load the observations of dataset "([^"]*)" in the year "([^"]*)"$"""){ (datast:String, year:Int) =>
	val dataset : Dataset = new Dataset
	dataset.id = datast
	dataset.year = year
	dataset.isCountryInRow = true
	observationDAO = new ObservationDAOImpl("files/TASTemplate.xlsx", true)
	observations = observationDAO.getObservationsByDataset(dataset)
  } 
  
  Given("""^I want to load the observations of non-existing dataset "([^"]*)" in the year "([^"]*)"$""") {(datast : String, year:Int) => {
    val dataset : Dataset = new Dataset
	dataset.id = datast
	dataset.isCountryInRow = true
	dataset.year = year
	observationDAO = new ObservationDAOImpl("files/TASTemplate.xlsx", true)
    intercept[IllegalArgumentException] {
      observations = observationDAO.getObservationsByDataset(dataset)
    }
  }}
  
  When("""^I check the value for the country "([^"]*)" and indicator "([^"]*)"$""") {(regionName : String, indicator : String) =>
    result = observations.find(obs => (obs.area.name.equals(regionName) && obs.indicator != null && obs.indicator.id.equals(indicator))).getOrElse(null)
    result should not be null
  }
  
  Then("""the value should be "([^"]*)"$""") { (value : Double) =>
    value should be(result.value.toDouble plusOrMinus 0.0000001f)
  }
  
  Then("""it should raise an Exception$""") { () =>
    throw new NotImplementedException
  }

}