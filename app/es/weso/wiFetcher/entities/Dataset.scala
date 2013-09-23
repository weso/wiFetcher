package es.weso.wiFetcher.entities

case class Dataset (
    val id:String=""
 ){
  
  override def equals(o : Any) = o match {
    case that : Dataset => that.id.equals(id)
    case _ => false
  }

}