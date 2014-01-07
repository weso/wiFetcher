package es.weso.wiFetcher.dao.entity

import es.weso.wiFetcher.dao.DAO

/**
 * This is a trait for all loaders that need a particular entity to load it's 
 * information
 */
trait EntityDAO[T] extends DAO[T]{

}