package es.weso.wiFetcher.dao

import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN
import org.scalatest.Matchers
import es.weso.wiFetcher.utils.StepsUtils

class ObservationDAOImplSteps extends ScalaDsl with EN with Matchers{
  
  Then("""the value should be "([^"]*)"$""") { (value : Double) =>
    val result = StepsUtils.vars.get(StepsUtils.VALUE).get
    result.toDouble should be(value)
  }
  
  Then("""the value should not be "([^"]*)"$""") { (value : Double) =>
    val result = StepsUtils.vars.get(StepsUtils.VALUE).get
    result.toDouble should not be(value)
  }

}