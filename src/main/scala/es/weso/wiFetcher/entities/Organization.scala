package es.weso.wiFetcher.entities

import java.net.URI

class Organization(val name:String, val uri:URI) {
	
  override def toString() : String = {
    new StringBuilder(name)
    	.append(" ").append(uri.toURL())
    	.toString
  }
  
}