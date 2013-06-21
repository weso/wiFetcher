package es.weso.wiFetcher.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.FileNotFoundException

@RunWith(classOf[JUnitRunner])
class IndicatorDAOImplSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{
  
  test("Try to load indicators information given a null path") {
    intercept[IllegalArgumentException] {
      new IndicatorDAOImpl(null, true)
    }
  }
  
  test("Try to load indicators information from a non-existing file") {
    intercept[FileNotFoundException] {
      new IndicatorDAOImpl("test.txt", true)
    }
  }

}