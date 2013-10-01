package es.weso.wiFetcher.persistence.jena

import es.weso.wiFetcher.persistence.ModelDAO
import com.hp.hpl.jena.rdf.model.Model


trait JenaModelDAO extends ModelDAO[Model] {
  
  def store(model : Model)

}