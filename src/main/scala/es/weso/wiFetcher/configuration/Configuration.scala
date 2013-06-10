package es.weso.wiFetcher.configuration

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

object Configuration {
  
  protected var CONFIG : CompositeConfiguration = null
  
  def getInitialCellSecondaryObservation() : String = {
    loadConfigure
    CONFIG.getString("initial_cell_secondary_indicator")
  }  
  
  def loadConfigure() = {
    if(CONFIG == null) {
      CONFIG = new CompositeConfiguration
      CONFIG.append(new PropertiesConfiguration("config/config.properties"))
    }
  }

}