package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Country

trait CountryDAO {
  
  def getCountries() : List[Country]

}