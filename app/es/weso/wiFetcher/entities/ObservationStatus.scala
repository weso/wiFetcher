package es.weso.wiFetcher.entities

object ObservationStatus extends Enumeration {
  type ObservationStatus = Value
  val Raw = Value("Raw")
  val Normalised = Value("Normalised")
  val Imputed = Value("Imputed")
  val Missed = Value("Missed")
  val Sorted = Value("Sorted")
  val Adjusted = Value("Adjusted")
  val Weighted = Value("Weighted")
  val Ordered = Value("Ordered")
}