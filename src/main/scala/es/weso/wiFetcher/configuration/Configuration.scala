package es.weso.wiFetcher.configuration

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

object Configuration {
  
  protected var CONFIG : CompositeConfiguration = null
  
  def getInitialCellSecondaryObservation() : String = {
    loadConfigure
    CONFIG.getString("initial_cell_secondary_indicator")
  }  
  
  def getIndicatorCell() : String = {
    loadConfigure
    CONFIG.getString("indicator_cell")
  }
  
  def getStatusCell() : String = {
    loadConfigure
    CONFIG.getString("status_cell")
  }
  
  def getCountryFile() : String = {
    loadConfigure
    CONFIG.getString("countries_file")
  }
  
  def getInitialCellIndicatorsSheet() : String = {
    loadConfigure
    CONFIG.getString("intial_cell_indicators_sheet")
  }
  
  def getIndicatorIdColumn() : Int = {
    loadConfigure
    CONFIG.getInt("indicator_column")
  }
  
  def getIndicatorSubindexColumn() : Int = {
    loadConfigure
    CONFIG.getInt("subindex_column")
  }
  
  def getIndicatorComponentColumn() : Int = {
    loadConfigure
    CONFIG.getInt("component_column")
  }
  
  def getIndicatorNameColumn() : Int = {
    loadConfigure
    CONFIG.getInt("name_column")
  }
  
  def getIndicatorDescriptionColumn() : Int = {
    loadConfigure
    CONFIG.getInt("description_column")
  }
  
  def getIndicatorSourceColumn() : Int = {
    loadConfigure
    CONFIG.getInt("source_column")
  }
  
  def getIndicatorProviderColumn() : Int = {
    loadConfigure
    CONFIG.getInt("provider_column")
  }
  
  def getIndicatorTypeColumn() : Int = {
    loadConfigure
    CONFIG.getInt("type_column")
  }
  
  def getIndicatorWeightColumn() : Int = {
    loadConfigure
    CONFIG.getInt("weight_column")
  }
  
  def getIndicatorHLColumn() : Int = {
    loadConfigure
    CONFIG.getInt("h/l_column")
  }
  
  def loadConfigure() = {
    if(CONFIG == null) {
      CONFIG = new CompositeConfiguration
      CONFIG.append(new PropertiesConfiguration("config/config.properties"))
      CONFIG.append(new PropertiesConfiguration("config/indicators.properties"))
    }
  }

}