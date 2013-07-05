package es.weso.wiFetcher.dao

import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.utils.FileUtils
import scala.io.Source

class DatasetDAOImpl(path : String, relativePath : Boolean) 
	extends DatasetDAO {
  
  private var datasets : List[Dataset] = load(FileUtils.getFilePath(
      path, relativePath))
      
  private def load(path : String) : List[Dataset] = {
    var datasets : ListBuffer[Dataset] = new ListBuffer[Dataset]
    val src = Source.fromFile(path)
    val iter = src.getLines
    iter.foreach(id => {
      var dataset = new Dataset
      dataset.id = id
      datasets += dataset
    })
    datasets.toList
  }
  
  def getDatasets() : List[Dataset] = {
    datasets
  }
}