package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Entity
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.traits.SubIndex

/**
 * This trait contains all method that has to have a class that load information
 * about subindexes and components
 */
trait SubIndexDAO extends DAO[Entity] {

  def getComponents(): List[Component]

  def getSubIndexes(): List[SubIndex]

}