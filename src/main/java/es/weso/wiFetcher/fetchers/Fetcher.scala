package es.weso.wiFetcher.fetchers

import sun.reflect.generics.reflectiveObjects.NotImplementedException

trait Fetcher {

  def fetch(x:String): Map[String, String] = {
    throw new NotImplementedException
  }
  
}