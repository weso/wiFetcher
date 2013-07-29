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
      
  /**
   *     This method load all dataset information from a file. Each line in the 
   *     file is one dataset.
   *     This implementation is temporary until the information that must have 
   *     a dataset is clear.
   *     At the moment, the file contains the id of the dataset, a boolean 
   *     indicating whether countries are in a row or in a column and the year
   *     corresponding to the dataset. You can find an example in src/main/
   * 	 resources/files/datasets.tsv 
   */    
  private def load(path : String) : List[Dataset] = {
    var datasets : ListBuffer[Dataset] = new ListBuffer[Dataset]
    val src = Source.fromFile(path)
    val iter = src.getLines.map(_.split("\t"))
    iter.foreach(line => {
      var dataset = new Dataset
      dataset.id = line(0)
      dataset.isCountryInRow = line(1).toBoolean
      dataset.year = line(2).toInt
      datasets += dataset
    })
    
    datasets.toList
  }
  
  /**
   * This method returns a list with datasets
   */
  def getDatasets() : List[Dataset] = {
    datasets
  }
}