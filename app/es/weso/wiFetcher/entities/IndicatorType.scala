package es.weso.wiFetcher.entities

object IndicatorType extends Enumeration {
  type IndicatorType = Value
  val Primary = Value("Primary")
  val Secondary = Value("Secondary")
  val Wrong = Value("Wrong")
}