package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.Entity
import es.weso.wiFetcher.entities.traits.SubIndex

trait SubIndexDAO extends DAO[Entity]{
  
  def getComponents() : List[Component]
  
  def getSubIndexes() : List[SubIndex]

}