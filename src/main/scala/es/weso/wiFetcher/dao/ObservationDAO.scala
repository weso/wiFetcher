package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.Dataset

trait ObservationDAO {
  
  def getObservations(datasets : List[Dataset]) : List[Observation]
  def getObservationsByIndicator(dataset : Dataset) : List[Observation]

}