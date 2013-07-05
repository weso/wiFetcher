package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Component
import es.weso.wiFetcher.entities.SubIndex

trait SubIndexDAO {
  
  def getComponents() : List[Component]
  
  def getSubIndexes() : List[SubIndex]

}