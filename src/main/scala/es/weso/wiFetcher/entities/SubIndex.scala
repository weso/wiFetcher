package es.weso.wiFetcher.entities

import scala.collection.mutable.ListBuffer

class SubIndex {
	
  var id : String = ""
  var name : String = ""
  var description : String = ""
  var weight : Double = 0.0
  private var components : ListBuffer[Component] = new ListBuffer[Component]
  
  def addComponent(component : Component) = {
    components += component
  }
  
  def getComponents() : List[Component] = {
    components.toList
  }
  
  override def equals(o : Any) : Boolean = {
    o match {
      case o:SubIndex => o.id.equals(this.id)
      case _ => false
    }
  }
  
}