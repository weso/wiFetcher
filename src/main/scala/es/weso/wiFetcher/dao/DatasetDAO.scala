package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Dataset

trait DatasetDAO {

  def getDatasets() : List[Dataset]
}