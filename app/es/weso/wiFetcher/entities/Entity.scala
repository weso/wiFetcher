package es.weso.wiFetcher.entities

import scala.collection.mutable.HashMap

case class Entity(
  val id: String = "",
  val names: HashMap[String, String] = new HashMap[String, String](),
  val descriptions: HashMap[String, String] = new HashMap[String, String](),
  val weight: Double = 0.0) 