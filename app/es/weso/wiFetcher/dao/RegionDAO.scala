package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Region

trait RegionDAO {
  
  def getRegions() : List[Region]

}