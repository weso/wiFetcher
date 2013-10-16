package es.weso.wiFetcher.configuration

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

object Configuration {

  protected val Config: CompositeConfiguration = loadConfigure

  def getInitialCellSecondaryObservation(): String = {
    Config.getString("initial_cell_secondary_indicator")
  }

  def getIndicatorCell(): String = {
    Config.getString("indicator_cell")
  }

  def getStatusCell(): String = {
    Config.getString("status_cell")
  }

  def getCountryFile(): String = {
    Config.getString("countries_file")
  }

  def getInitialCellIndicatorsSheet(): String = {
    Config.getString("intial_cell_indicators_sheet")
  }

  def getIndicatorIdColumn(): Int = {
    Config.getInt("indicator_column")
  }

  def getIndicatorComponentColumn(): Int = {
    Config.getInt("component_column")
  }

  def getIndicatorNameColumn(): Int = {
    Config.getInt("name_column")
  }

  def getIndicatorDescriptionColumn(): Int = {
    Config.getInt("description_column")
  }

  def getIndicatorSourceColumn(): Int = {

    Config.getInt("source_column")
  }

  def getIndicatorProviderColumn(): Int = {

    Config.getInt("provider_column")
  }

  def getIndicatorTypeColumn(): Int = {

    Config.getInt("type_column")
  }

  def getIndicatorWeightColumn(): Int = {

    Config.getInt("weight_column")
  }

  def getIndicatorHLColumn(): Int = {

    Config.getInt("h/l_column")
  }

  def getIndicatorFilename(): String = {

    Config.getString("indicator_filename")
  }

  def getObservationFile(): String = {

    Config.getString("observations_file")
  }

  def getDatasetFile(): String = {

    Config.getString("dataset_file")
  }

  def getSubindexFile(): String = {
    Config.getString("subindexes_file")
  }

  def getSubindexInitialCell(): String = {
    Config.getString("initial_cell")
  }

  def getTypeColumn(): Int = {

    Config.getInt("sb_type_column")
  }

  def getIdColumn(): Int = {
    Config.getInt("sb_id_column")
  }

  def getSubindexWeithColumn(): Int = {
    Config.getInt("sb_weight_column")
  }

  def getSubindexNameColumn(): Int = {
    Config.getInt("sb_name_column")
  }

  def getSubindexDescriptionColumn(): Int = {
    Config.getInt("sb_description_column")
  }

  def getRegionsFilename(): String = {
    Config.getString("regions_filename")
  }

  def getRegionInitialCell(): String = {
    Config.getString("intial_cell_regions_sheet")
  }

  def getRegionNameColumn(): Int = {
    Config.getInt("region_name_column")
  }

  def getRegionCountryColumn(): Int = {
    Config.getInt("region_country_column")
  }

  def getCountryReconciliatorFile(): String = {
    Config.getString("country_reconciliator_file")
  }

  def getIndicatorStopWordsFile(): String = {
    Config.getString("indicator_stop_words_recon")
  }

  def getProvierInitialCell(): String = {
    Config.getString("initial_cell_providers")
  }

  def getProviderIdColumn(): Int = {
    Config.getInt("provider_id_column")
  }

  def getProviderNameColumn(): Int = {
    Config.getInt("provider_name_column")
  }

  def getProviderWebColumn(): Int = {
    Config.getInt("provider_web_column")
  }

  def getProviderSourceColumn(): Int = {
    Config.getInt("provider_source_column")
  }
  
  def getVirtuosoServer() : String = {
    Config.getString("virt_server")
  }
  
  def getVirtuosoUser() : String = {
    Config.getString("virt_user")
  }
  
  def getVirtuosoPass() : String = {
    Config.getString("virt_pass")
  }
  
  def getInicialCellPrimaryIndicator() : String = {
    Config.getString("initial_cell_primary_indicator")
  }
  
  def getPrimaryIndicatorRow() : Int = {
    Config.getInt("primary_indicator_row")
  }
  
  def getPrimaryIndicatorLastCol() : Int = {
    Config.getInt("primary_indicator_last_col")
  }
  
  def getVirtuosoLoadDir() : String = {
    Config.getString("load_dir")
  }
  
  def getVirtuosoSourceDir() : String = {
    Config.getString("source_dir")
  }

  def loadConfigure() = {
    val config = new CompositeConfiguration
    config.append(new PropertiesConfiguration("conf/countries.properties"))
    config.append(new PropertiesConfiguration("conf/indicators.properties"))
    config.append(new PropertiesConfiguration("conf/observations.properties"))
    config.append(new PropertiesConfiguration("conf/datasets.properties"))
    config.append(new PropertiesConfiguration("conf/subindexes.properties"))
    config.append(new PropertiesConfiguration("conf/regions.properties"))
    config.append(new PropertiesConfiguration("conf/providers.properties"))
    config.append(new PropertiesConfiguration("conf/virtuoso.properties"))
    config
  }

}