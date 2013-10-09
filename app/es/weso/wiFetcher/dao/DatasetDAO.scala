package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Dataset
import scala.collection.mutable.ListBuffer

trait DatasetDAO  extends DAO[Dataset]{

  def getDatasets() : List[Dataset]
}