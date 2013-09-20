package es.weso.wiFetcher.entities.traits

import es.weso.wiFetcher.entities.Entity
import scala.collection.mutable.ListBuffer

trait SubIndex extends Entity {

  private val components: ListBuffer[Component] = new ListBuffer[Component]

  def addComponent(component: Component) = {
    components += component
  }

  def getComponents(): List[Component] = {
    components.toList
  }

  override def equals(o: Any): Boolean = {
    o match {
      case o: SubIndex => o.id.equals(this.id)
      case _ => false
    }
  }

}