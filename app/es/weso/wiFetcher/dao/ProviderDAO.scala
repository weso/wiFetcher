package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Provider

/**
 * This trait contains all method that has to have a class that load information
 * about providers
 */
trait ProviderDAO  extends DAO[Provider] {
  
  def getProviders() : List[Provider]

}