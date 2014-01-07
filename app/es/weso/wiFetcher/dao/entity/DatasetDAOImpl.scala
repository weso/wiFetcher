package es.weso.wiFetcher.dao.entity

import es.weso.wiFetcher.dao.DatasetDAO
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import scala.collection.mutable.ListBuffer

/**
 * This class provide to the application the functionality of load datasets 
 * information
 */
class DatasetDAOImpl(indicators : List[Indicator]) extends DatasetDAO 
	with EntityDAO[Dataset]{
  
  private val datasets : ListBuffer[Dataset] = loadDatasets(indicators) 

  /**
   * This method generates all datasets from a list of indicators
   */
  private def loadDatasets(indicators : List[Indicator]) : ListBuffer[Dataset] = {
    //This variable contains a list of lists with all datasets
    val result = for(indicator <- indicators) yield {
      Seq(Dataset(indicator.id + "-Ordered"), 
          Dataset(indicator.id + "-Imputed"))
    }
    //this sentence combines all lists of "result" in one
    result.foldLeft(ListBuffer.empty[Dataset])((a, b) => a ++= b)
  }
  
  /**
   * This method returns a list with all datasets
   */
  def getDatasets() : List[Dataset] = {
    datasets.toList
  }
}