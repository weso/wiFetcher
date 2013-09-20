package es.weso.wiFetcher.dao.poi

import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.io.Source
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.utils.FileUtils
import DatasetDAOImpl.logger
import es.weso.wiFetcher.dao.DatasetDAO
/**
 * This class contains the implementation that allows to load the information
 * about datasets of the Web Index
 *
 *  At the moment, Web Foundation does not define the structure of files and
 * temporally, we use a TSV file that contains the minimum information about
 * dataset. Their identifier, year and a boolean that indicates where we can
 * find the countries name in observations spreadsheets.
 */

class DatasetDAOImpl(path: String, relativePath: Boolean) extends DatasetDAO {
  
  import DatasetDAOImpl._
  
  private var datasets: List[Dataset] = load(FileUtils.getFilePath(
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
   *   @param path The path of the file that contains the information
   *   @return A list with all datasets loaded
   */
  private def load(path: String): List[Dataset] = {
    var datasets: ListBuffer[Dataset] = new ListBuffer[Dataset]
    val src = Source.fromFile(path)
    val iter = src.getLines.map(_.split("\t"))
    logger.info("Begin dataset extraction")
    iter.foreach(line => {
      var dataset = new Dataset
      dataset.id = line(0)
      datasets += dataset
    })
    logger.info("Finish dataset extraction")
    datasets.toList
  }

  /**
   * This method returns a list with datasets
   */
  def getDatasets(): List[Dataset] = {
    datasets
  }

}

object DatasetDAOImpl {
  
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
  
}