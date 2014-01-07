package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Region

/**
 * This trait contains all method that has to have a class that load information
 * about regions
 */
trait RegionDAO  extends DAO[Region]{
  
  def getRegions() : List[Region]

}