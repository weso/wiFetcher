package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Indicator

trait IndicatorDAO extends DAO [Indicator] {
  
  def getPrimaryIndicators() : List[Indicator]
  
  def getSecondaryIndicators() : List[Indicator]

}