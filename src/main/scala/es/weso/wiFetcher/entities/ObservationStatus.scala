package es.weso.wiFetcher.entities

object ObservationStatus extends Enumeration {
	type ObservationStatus = Value
	val Raw = Value("Raw")
	val Normalised = Value("Normalised")
	val Imputed = Value("Imputed")
	val Missed = Value("Missed")
  
}