package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Country

trait CountryDAO extends DAO[Country] {
  
  def getCountries() : List[Country]

}