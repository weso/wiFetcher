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

@RunWith(classOf[JUnitRunner])
class DatasetDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  val indicators : ListBuffer[Indicator] = {
    val list :ListBuffer[Indicator] = ListBuffer.empty
    list += Indicator("A", 
        IndicatorType.Primary, 
        "test indicator",
        "test indicator description",
        null,
        null,
        0,
        0.5,
        IndicatorHighLow.High,
        "",
        null,
        "")
    list += Indicator("B", 
        IndicatorType.Primary, 
        "test indicator 2",
        "test indicator description 2",
        null,
        null,
        0,
        0.5,
        IndicatorHighLow.High,
        "",
        null,
        "")
    list += Indicator("C", 
        IndicatorType.Primary, 
        "test indicator 3",
        "test indicator description 3",
        null,
        null,
        0,
        0.5,
        IndicatorHighLow.High,
        "",
        null,
        "")
    list
  }
  
  
  test("Load correct all datasets and verify that all data is loaded") {
    val datasetDao : DatasetDAOImpl = new DatasetDAOImpl(indicators.toList)
    val datasets : List[Dataset] = datasetDao.getDatasets
    datasets.size should be (9)
    indicators.foreach(indicator => {
      datasets.contains(Dataset(indicator.id + "-Ordered")) should be (true)
      datasets.contains(Dataset(indicator.id + "-Imputed")) should be (true)
      datasets.contains(Dataset(indicator.id + "-Normalised")) should be (true)
    })
  }

}