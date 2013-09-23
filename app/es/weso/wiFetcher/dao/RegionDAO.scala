package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Region

trait RegionDAO  extends DAO[Region]{
  
  def getRegions() : List[Region]

}