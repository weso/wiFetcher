package es.weso.wiFetcher.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers
import org.scalatest.FunSuite
import java.io.FileNotFoundException
import es.weso.wiFetcher.entities.Dataset

@RunWith(classOf[JUnitRunner])
class DatasetDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{
  
  test("Try to load datasets specifying a null path") {
    intercept[IllegalArgumentException] {
      new DatasetDAOImpl(null, true)
    }
  }
  
  test("Try to load datasets from a non-existing file") {
    intercept[FileNotFoundException] {
      new DatasetDAOImpl("files/test.tsv", true)
    }
  }
  
  test("Load correct all datasets and verify that all data is loaded") {
    val datasetDao : DatasetDAOImpl = new DatasetDAOImpl("files/datasets.tsv", 
        true)
    val datasets : List[Dataset] = datasetDao.getDatasets
    datasets.size should be (5)
    datasets.foreach(dataset => {
      if(dataset.id == null || dataset.id.equals(""))
        throw new Exception("Any dataset is no loaded correctly")
    })
  }

}