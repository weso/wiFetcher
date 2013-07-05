package es.weso.wiFetcher.entities

class Dataset {
  
  var id : String = ""
    
  override def equals(o : Any) = o match {
    case that : Dataset => that.id.equals(id)
    case _ => false
  }

}