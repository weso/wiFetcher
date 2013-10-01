package es.weso.wiFetcher.persistence.jena

import com.hp.hpl.jena.rdf.model.Model
import virtuoso.jena.driver.VirtModel
import es.weso.wiFetcher.configuration.Configuration

object JenaModelDAOImpl extends JenaModelDAO {
  
  def store(model : Model) = {
    val timeStamp = System.currentTimeMillis / 1000
    val m : Model = VirtModel.openDatabaseModel(timeStamp.toString, 
        Configuration.getVirtuosoServer, 
        Configuration.getVirtuosoUser,
        Configuration.getVirtuosoPass)
    val statements = model.listStatements    
    while(statements.hasNext()) {
      m.add(statements.nextStatement)
    }
    m.close
  }

}