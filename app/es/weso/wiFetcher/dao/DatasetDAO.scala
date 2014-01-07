package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Dataset
import scala.collection.mutable.ListBuffer

/**
 * This trait contains all method that has to have a class that load information
 * about datasets
 */
trait DatasetDAO  extends DAO[Dataset]{

  def getDatasets() : List[Dataset]
}