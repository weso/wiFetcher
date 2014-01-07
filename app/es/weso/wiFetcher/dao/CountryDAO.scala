package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Country

/**
 * This trait contains all method that has to have a class that load information
 * about countries
 */
trait CountryDAO extends DAO[Country] {
  
  def getCountries() : List[Country]

}