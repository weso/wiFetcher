package es.weso.wiFetcher.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers
import org.scalatest.FunSuite
import java.io.FileNotFoundException
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.dao.entity.DatasetDAOImpl
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.IndicatorType
import es.weso.wiFetcher.entities.IndicatorHighLow
import es.weso.wiFetcher.entities.Provider
import scala.collection.mutable.HashMap

@RunWith(classOf[JUnitRunner])
class DatasetDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  val indicators : ListBuffer[Indicator] = {
    val list :ListBuffer[Indicator] = ListBuffer.empty
    list += Indicator("A", 
        IndicatorType.Primary, 
        HashMap("en" -> "test indicator"),
        HashMap("en" -> "test indicator description"),
        null,
        null,
        0,
        0.5,
        IndicatorHighLow.High,
        "",
        null,
        ListBuffer(Provider("", "", "", "")))
    list += Indicator("B", 
        IndicatorType.Primary, 
        HashMap("en" -> "test indicator 2"),
        HashMap("en" -> "test indicator description 2"),
        null,
        null,
        0,
        0.5,
        IndicatorHighLow.High,
        "",
        null,
        ListBuffer(Provider("", "", "", "")))
    list += Indicator("C", 
        IndicatorType.Primary, 
        HashMap("en" -> "test indicator 3"),
        HashMap("en" -> "test indicator description 3"),
        null,
        null,
        0,
        0.5,
        IndicatorHighLow.High,
        "",
        null,
        ListBuffer(Provider("", "", "", "")))
    list
  }
  
  
  test("Load correct all datasets and verify that all data is loaded") {
    val datasetDao : DatasetDAOImpl = new DatasetDAOImpl(indicators.toList)
    val datasets : List[Dataset] = datasetDao.getDatasets
    datasets.size should be (6)
    indicators.foreach(indicator => {
      datasets.contains(Dataset(indicator.id + "-Ordered")) should be (true)
      datasets.contains(Dataset(indicator.id + "-Imputed")) should be (true)
    })
  }

}