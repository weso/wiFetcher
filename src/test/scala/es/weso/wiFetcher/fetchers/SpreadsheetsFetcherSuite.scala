package es.weso.wiFetcher.fetchers

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import java.io.FileNotFoundException


@RunWith(classOf[JUnitRunner])
class SpreadsheetsFetcherSuite extends FunSuite with BeforeAndAfter 
	with ShouldMatchers{
  
  var fetcher : SpreadsheetsFetcher = null
  
  before {
    fetcher = new SpreadsheetsFetcher
  }
  
  test("Try to load a non-existent spreadsheet") {
    intercept[FileNotFoundException]{
      fetcher.loadWorkbook("files/test.xlsx", true)
    }
  }

}