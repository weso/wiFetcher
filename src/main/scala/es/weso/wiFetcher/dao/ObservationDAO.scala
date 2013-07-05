package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.ObservationStatus._

trait ObservationDAO {
  
  def getObservations(datasets : List[Dataset]) : List[Observation]
  def getObservationsByIndicator(dataset : Dataset) : List[Observation]

}