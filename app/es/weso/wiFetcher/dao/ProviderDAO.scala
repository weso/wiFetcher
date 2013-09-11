package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Provider

trait ProviderDAO {
  
  def getProviders() : List[Provider]

}