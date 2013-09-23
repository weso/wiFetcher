package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Provider

trait ProviderDAO  extends DAO[Provider] {
  
  def getProviders() : List[Provider]

}