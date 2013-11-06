package es.weso.wiFetcher.dao

import java.io.FileInputStream
import scala.collection.mutable.ListBuffer
import org.junit.runner.RunWith
import org.scalatest.Matchers
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.utils.FileUtils
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.dao.poi.IndicatorDAOImpl
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import java.io.File

class IndicatorDAOImplSteps extends ScalaDsl with EN with Matchers{

  val fetcher : SpreadsheetsFetcher = SpreadsheetsFetcher(
      new File(FileUtils.getFilePath("files/Structure.xlsx", true)), 
      new File(FileUtils.getFilePath("files/Raw.xlsx", true)))
  var indicatorDao : IndicatorDAO = null
  val indicators : ListBuffer[Indicator] = ListBuffer.empty
  var result : Indicator = null
  
  Given("""^I want to load all information about indicators in the WebIndex$""") { () =>
    val is = new FileInputStream(FileUtils.getFilePath("files/Structure.xlsx", 
        true))
    indicatorDao = new IndicatorDAOImpl(is)(fetcher)
    indicators ++= indicatorDao.getPrimaryIndicators
    indicators ++= indicatorDao.getSecondaryIndicators
  }
  
  When("""^I check the indicator with "([^"]*)" "([^"]*)"$""") { (property : String, value : String) =>
    result = property match {
      case "id" => indicators.find(indicator => indicator.id.equals(value)).getOrElse(throw new IllegalArgumentException("There is no indicator with id " + value))
      case "name" => indicators.find(indicator => indicator.labels.get("en").get.equals(value)).getOrElse(throw new IllegalArgumentException("There is no indicator with name " + value))
      case "description" => indicators.find(indicator => indicator.comments.get("en").get.equals(value)).getOrElse(throw new IllegalArgumentException("There is no indicator with description " + value))
      case _ => throw new IllegalArgumentException("")
    }
  }
  
  Then("""^the indicator "([^"]*)" should be "([^"]*)"$""") { (property : String, value : String) =>
    property match {
      case "id" => result.id should be (value)
      case "name" => result.labels.get("en").get should be (value)
      case "description" => result.comments.get("en").get should be (value)
      case "type" => result.indicatorType.toString() should be(value)
      case "weight" => result.weight should be (value.toDouble +- 0.0000001f)
      case "source" => result.source should be (value)
      case "hl" => result.highLow.toString should be (value)
      case _ => throw new IllegalArgumentException("")
    }
  }
  
  Then("""^the indicator "([^"]*)" should not be "([^"]*)"$""") {(property : String, value : String) =>
    property match {
      case "id" => result.id should not be (value)
      case "name" => result.labels.get("en").get should not be (value)
      case "description" => result.comments.get("en").get should not be (value)
      case "type" => result.indicatorType should not be(value)
      case "weight" => result.weight should not be (value.toDouble +- 0.0000001f)
      case "source" => result.source should not be (value)
      case "hl" => result.highLow should not be (value)
      case _ => throw new IllegalArgumentException("")
    }
  }
  
  Then("""^the number of primary indicators should be "([^"]*)"$""") {(indicators : Int) =>
    indicatorDao.getPrimaryIndicators.size should be (indicators)
  }
  
  Then("""^the number of secondary indicators should be "([^"]*)"$""") {(indicators : Int) =>
    indicatorDao.getSecondaryIndicators.size should be (indicators)
  }
  
  
}