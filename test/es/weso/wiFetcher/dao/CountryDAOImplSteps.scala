package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.Matchers
import es.weso.wiFetcher.entities.Country
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.dao.file.CountryDAOImpl

@RunWith(classOf[JUnitRunner])
class CountryDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  var countryDao : CountryDAO = null
  var result : Country = null
  
  Given("""^I want to load names and iso-codes for all countries presents in WebIndex$""") { () =>
    countryDao = new CountryDAOImpl("files/countryCodes.tsv", true)(null)
  }
  
  When("""^I check the country with the "([^"]*)" "([^"]*)"$""") { (comparator : String, value : String) =>
    var countries = countryDao.getCountries
    comparator match {
      case "name" => result = countries.find(c => c.name.equals(value)).getOrElse(null)
      case "iso2-code" => result = countries.find(c => c.iso2Code.equals(value)).getOrElse(null)
      case "iso3-code" => result = countries.find(c => c.iso3Code.equals(value)).getOrElse(null)
      case _ => throw new IllegalArgumentException("Comparator " + comparator 
          + " is not valid")
    }
  }
  
  Then("""^the country "([^"]*)" should be "([^"]*)"$""") { (comparator : String, value : String) =>
    comparator match {
      case "iso2-code" => result.iso2Code should be (value)
      case "iso3-code" => result.iso3Code should be (value)
      case "name" => result.name should be (value)
      case _ => throw new IllegalArgumentException("Comparator " + comparator 
          + " is not valid")
    }
  }
  
  Then("""the country result should be null$""") { () =>
    result should be (null)
  }

}