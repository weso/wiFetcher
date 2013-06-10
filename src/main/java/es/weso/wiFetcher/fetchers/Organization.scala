package es.weso.wiFetcher.fetchers

import java.net.URI

class Organization(val name:String, val uri:URI) {
	
  override def toString() : String = {
    new StringBuilder(name)
    	.append(" ").append(uri.toURL())
    	.toString
  }
  
}