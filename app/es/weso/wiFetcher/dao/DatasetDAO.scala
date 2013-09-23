package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Dataset

trait DatasetDAO  extends DAO[Dataset]{

  def getDatasets() : List[Dataset]
}