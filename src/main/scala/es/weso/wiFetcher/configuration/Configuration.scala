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
  
  def getIndicatorFilename() : String = {
    loadConfigure
    CONFIG.getString("indicator_filename")
  }
  
  def getObservationFile() : String = {
    loadConfigure
    CONFIG.getString("observations_file")
  }
  
  def getDatasetFile() : String = {
    loadConfigure
    CONFIG.getString("dataset_file")
  }
  
  def getSubindexFile() : String = {
    loadConfigure
    CONFIG.getString("subindexes_file")
  }
  
  def getSubindexInitialCell() : String = {
    loadConfigure
    CONFIG.getString("initial_cell")
  }
  
  def getSubindexColumn() : Int = {
    loadConfigure
    CONFIG.getInt("sb_subindex_column")
  }
  
  def getComponentColumn() : Int = {
    loadConfigure
    CONFIG.getInt("sb_component_column")
  }
  
  def getSubindexWeithColumn() : Int = {
    loadConfigure
    CONFIG.getInt("sb_weight_column")
  }
  
  def getSubindexNameColumn() : Int = {
    loadConfigure
    CONFIG.getInt("sb_name_column")
  }
  
  def getSubindexDescriptionColumn() : Int = {
    loadConfigure
    CONFIG.getInt("sb_description_column")
  }
  
  def getRegionsFilename() : String = {
    loadConfigure
    CONFIG.getString("regions_filename")
  }
  
  def getRegionInitialCell() : String = {
    loadConfigure
    CONFIG.getString("intial_cell_regions_sheet")
  }
  
  def getRegionNameColumn() : Int = {
    loadConfigure
    CONFIG.getInt("region_name_column")
  }
  
  def getRegionCountryColumn() : Int = {
    loadConfigure
    CONFIG.getInt("region_country_column")
  }
  
  def getCountryReconciliatorFile() : String = {
    loadConfigure
    CONFIG.getString("country_reconciliator_file")
  }
  
  def getIndicatorStopWordsFile() : String = {
    loadConfigure
    CONFIG.getString("indicator_stop_words_recon")
  }
  
  def loadConfigure() = {
    if(CONFIG == null) {
      CONFIG = new CompositeConfiguration
      CONFIG.append(new PropertiesConfiguration("config/countries.properties"))
      CONFIG.append(new PropertiesConfiguration("config/indicators.properties"))
      CONFIG.append(new PropertiesConfiguration("config/observations.properties"))
      CONFIG.append(new PropertiesConfiguration("config/datasets.properties"))
      CONFIG.append(new PropertiesConfiguration("config/subindexes.properties"))
      CONFIG.append(new PropertiesConfiguration("config/regions.properties"))
    }
  }

}