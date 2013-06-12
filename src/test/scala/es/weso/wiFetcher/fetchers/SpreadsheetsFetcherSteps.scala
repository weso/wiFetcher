package es.weso.wiFetcher.fetchers

import org.scalatest.matchers.ShouldMatchers
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Observation
import sun.reflect.generics.reflectiveObjects.NotImplementedException

class SpreadsheetsFetcherSteps extends ScalaDsl with EN with ShouldMatchers {
  
  var observations : List[Observation] = null
  var result : Observation = null
  
  Given("""^I want to load the observations of indicator "([^"]*)"$"""){ (indicator:String) =>
	var fetcher : SpreadsheetsFetcher = new SpreadsheetsFetcher
	fetcher.loadWorkbook("files/rawdata.xlsx", true)
	var dataset : Dataset = new Dataset
	dataset.id = indicator
	observations = fetcher.extractObservationsByDataset(dataset)
  }  
  
  When("""^I check the value for the country "([^"]*)" in the year "([^"]*)"$""") {(regionName : String, year : Int) =>
    result = observations.find(obs => (obs.area.name.equals(regionName) && obs.year == year)).getOrElse(null)
    result should not be null
  }
  
  Then("""the value should be "([^"]*)"$""") { (value : Double) =>
    value should be(result.value.toDouble plusOrMinus 0.0000001f)
  }
  
  Then("""it should raise an Exception$""") { () =>
    throw new NotImplementedException
  }
  
  

}