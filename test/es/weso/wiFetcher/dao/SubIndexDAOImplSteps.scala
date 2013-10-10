package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.Matchers
import es.weso.wiFetcher.entities.traits.SubIndex
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.FileUtils
import java.io.FileInputStream
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.dao.poi.SubIndexDAOImpl

@RunWith(classOf[JUnitRunner])
class SubIndexDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  var subIndexDAO : SubIndexDAO = null
  var subIndexes : List[SubIndex] = null
  var components : List[Component] = null
  var subIndex : SubIndex = null
  var component : Component = null
  
  Given("""^I want to load all information about subindexes in the WebIndex$""") {() => 
    
    subIndexDAO = new SubIndexDAOImpl(new FileInputStream(
        FileUtils.getFilePath("files/structure.xlsx", true)))(null)
    subIndexDAO should not be (null)
  }
  
  When("""^I check all subindexes and components are loaded$""") {() => 
    subIndexes = subIndexDAO.getSubIndexes
    components = subIndexDAO.getComponents
    components.size should not be (0)
    subIndexes.size should not be (0)
  }
  
  When("""^I check the subindex with "([^"]*)" "([^"]*)"$""") { (property : String, value : String) =>
    subIndex = property match {
      case "name" => subIndexes.find(subindex => subindex.name.equals(value)).getOrElse(throw new IllegalArgumentException("There is no subindex with name " + value))
      case "id" => subIndexes.find(subindex => subindex.id.equals(value)).getOrElse(throw new IllegalArgumentException("There is no subindex with id " + value))
      case _ => throw new IllegalArgumentException
    }
  }
  
  When("""^I check the numbers of components of subindex "([^"]*)"$""") {(subindex : String) =>
    subIndex = subIndexDAO.getSubIndexes.find(sub => sub.id.equals(subindex)).getOrElse(throw new IllegalArgumentException)
  }
  
  When("""^I check the number of indicators of component "([^"]*)"$""") {(component : String) =>
    this.component = null
  }
  
  When("""^I check the component with "([^"]*)" "([^"]*)"$""") { (property : String, value : String) =>
    component = property match {
      case "name" => components.find(comp => comp.name.equals(value)).getOrElse(throw new IllegalArgumentException("There is no subindex with name " + value))
      case "id" => components.find(comp => comp.id.equals(value)).getOrElse(throw new IllegalArgumentException("There is no subindex with id " + value))
      case _ => throw new IllegalArgumentException
    }
  }
  
  Then("""^the number of indicators should be "([^"]*)"$""") {(indicators : Int) =>
    component.getIndicators.size should be (indicators)
  }
  
  Then("""^the component "([^"]*)" should be "([^"]*)"$""") { (property : String, value : String) =>
    property match {
      case "name" => component.name should be (value)
      case "id" => component.id should be (value)
      case "description" => component.description should be (value)
      case "weight" => component.weight should be (value.toDouble +- 0.0000001f)
    }
  }
  
  Then("""^the number of components should be "([^"]*)"$""") {(comps : Int) =>
    subIndex.getComponents.size should be (comps)
  }
  
  Then("""^There are "([^"]*)" subindexes and "([^"]*)" components$""") { (subindexes : Int, comps : Int) =>
    subIndexes.size should be (subindexes)
    components.size should be (comps)
  }
  
  Then("""^the subindex "([^"]*)" should be "([^"]*)"$""") {(property : String, value : String) =>
    property match {
      case "name" => subIndex.name should be (value)
      case "id" => subIndex.id should be (value)
      case "description" => subIndex.description should be (value)
      case "weight" => subIndex.weight should be (value.toDouble +- 0.0000001f)
    }
  }

}