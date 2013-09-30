package es.weso.wiFetcher.dao.entity

import es.weso.wiFetcher.dao.DatasetDAO
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import scala.collection.mutable.ListBuffer

class DatasetDAOImpl(indicators : List[Indicator]) extends DatasetDAO 
	with EntityDAO[Dataset]{
  
  private val datasets : ListBuffer[Dataset] = loadDatasets(indicators) 

  private def loadDatasets(indicators : List[Indicator]) : ListBuffer[Dataset] = {
    val result = for(indicator <- indicators) yield {
      Seq(Dataset(indicator.id + "-Ordered"), 
          Dataset(indicator.id + "-Imputed"), 
          Dataset(indicator.id + "-Normalised"))
    }
    result.foldLeft(ListBuffer.empty[Dataset])((a, b) => a ++= b)
  }
  
  def getDatasets() : List[Dataset] = {
    datasets.toList
  }
}