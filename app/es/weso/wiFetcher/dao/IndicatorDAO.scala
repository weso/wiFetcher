package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Indicator

/**
 * This trait contains all method that has to have a class that load information
 * about indicators
 */
trait IndicatorDAO extends DAO [Indicator] {
  
  def getPrimaryIndicators() : List[Indicator]
  
  def getSecondaryIndicators() : List[Indicator]

}