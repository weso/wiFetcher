package es.weso.wiFetcher.entities.traits

import es.weso.wiFetcher.entities.Entity
import scala.collection.mutable.ListBuffer

trait Index extends Entity{
  
  private val subindexes: ListBuffer[SubIndex] = new ListBuffer[SubIndex]

  def addSubindex(subindex: SubIndex) = {
    subindexes += subindex
  }
  
  def addSubindexes(subindexes : ListBuffer[SubIndex]) = {
    this.subindexes ++= subindexes
  }

  def getSubindexes(): List[SubIndex] = {
    subindexes.toList
  }

  override def equals(o: Any): Boolean = {
    o match {
      case o: Index => o.id.equals(this.id)
      case _ => false
    }
  }

}