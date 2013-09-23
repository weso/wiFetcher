package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.ObservationStatus._
import es.weso.reconciliator.CountryReconciliator
import es.weso.wiFetcher.configuration.Configuration

trait ObservationDAO  extends DAO[Observation]{
  
  val reconciliator : CountryReconciliator = new CountryReconciliator(
      Configuration.getCountryReconciliatorFile, true)
  
  def getObservations(datasets : List[Dataset]) : List[Observation]
  def getObservationsByDataset(dataset : Dataset) : List[Observation]

}