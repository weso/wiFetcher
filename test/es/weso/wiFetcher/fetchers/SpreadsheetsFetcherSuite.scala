package es.weso.wiFetcher.fetchers

import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSuite
import scala.collection.JavaConversions._
import org.scalatest.junit.JUnitRunner
import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration
import java.io.File
import es.weso.wiFetcher.utils.FileUtils


@RunWith(classOf[JUnitRunner])
class SpreadsheetsFetcherSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  val config = new CompositeConfiguration
    config.append(new PropertiesConfiguration("conf/test.properties"))
    
  val structureFile = config.getString("structure_file")
  val observationFile = config.getString("observations_file")
  val indicators = config.getInt("indicators")
  val observations = config.getInt("observations")
  val components = config.getInt("components")
  val subindexes = config.getInt("subindexes")
  val datasets = config.getInt("datasets")

  test("Load structure and information file and check the number of " +
  		"observations, indicators, components, subindexes and datasets") {
    val fetcher : SpreadsheetsFetcher = SpreadsheetsFetcher(
      new File(FileUtils.getFilePath(structureFile, true)),
      new File(FileUtils.getFilePath(observationFile, true)))
    fetcher.components.size should be (components)
    fetcher.datasets.size should be (datasets)
    fetcher.subIndexes.size should be (subindexes)
    fetcher.observations.size should be (observations)
    val totalIndicators = fetcher.primaryIndicators.size + 
    	fetcher.secondaryIndicators.size
	totalIndicators should be (indicators)
    val obsByDataset = fetcher.observations.groupBy(observation => observation.dataset)
    fetcher.datasets.foreach(dataset => {
      obsByDataset.get(dataset.id).isEmpty should be (false)
    })
  }
}