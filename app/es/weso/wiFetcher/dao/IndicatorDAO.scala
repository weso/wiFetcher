package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Indicator

trait IndicatorDAO {
  
  def getPrimaryIndicators() : List[Indicator]
  
  def getSecondaryIndicators() : List[Indicator]

}