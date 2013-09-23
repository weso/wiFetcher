package es.weso.wiFetcher.fetchers

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import scala.collection.mutable.ListBuffer
import org.apache.log4j.Logger
import es.weso.reconciliator.CountryReconciliator
import es.weso.wiFetcher.analyzer.indicator.IndicatorReconciliator
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.file.CountryDAOImpl
import es.weso.wiFetcher.dao.file.DatasetDAOImpl
import es.weso.wiFetcher.dao.poi.IndicatorDAOImpl
import es.weso.wiFetcher.dao.poi.ObservationDAOImpl
import es.weso.wiFetcher.dao.poi.ProviderDAOImpl
import es.weso.wiFetcher.dao.poi.RegionDAOImpl
import es.weso.wiFetcher.dao.poi.SubIndexDAOImpl
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.ObservationStatus.ObservationStatus
import es.weso.wiFetcher.entities.Provider
import es.weso.wiFetcher.entities.Region
import es.weso.wiFetcher.entities.traits.SubIndex
import com.hp.hpl.jena.assembler.exceptions.NoImplementationException

object SpreadsheetsFetcher extends Fetcher {

  var components: List[Component] = null
  var subIndexes: List[SubIndex] = null
  var primaryIndicators: List[Indicator] = null
  var secondaryIndicators: List[Indicator] = null
  var countries: List[Country] = null
  var regions: List[Region] = null
  var providers: List[Provider] = null
  //Create an indicator reconciliator
  val indicatorReconciliator: IndicatorReconciliator =
    new IndicatorReconciliator
  var datasets: List[Dataset] = null
  var observations: List[Observation] = null

  private val logger: Logger = Logger.getLogger(this.getClass())
  private val countryReconciliator: CountryReconciliator =
    new CountryReconciliator(Configuration.getCountryReconciliatorFile, true)

  /**
   * This method load all structure about Web Index information
   */
  def loadStructure(f: File) {
    safeLoadInformation(f, loadSubIndexInformation)
    safeLoadInformation(f, loadIndicatorInformation)
    loadCountryInformation(Configuration.getCountryFile, true)
    safeLoadInformation(f, loadRegionInformation)
    safeLoadInformation(f, loadProviderInformation)
  }

  /**
   * This method load all observation form an excel file
   */
  def loadObservations(f: File) {
    loadDatasetInformation(Configuration.getDatasetFile, true)
    safeLoadInformation(f, loadObservationInformation)
  }

  private def loadDatasetInformation(path: String, relativePath: Boolean) {
    val datasetDao = new DatasetDAOImpl(path, relativePath)
    datasets = datasetDao.getDatasets
  }

  private def loadObservationInformation(is: InputStream) {
    val observationDao = new ObservationDAOImpl(is)
    observations = observationDao.getObservations
  }

  def safeLoadInformation(file: File, proccess: (InputStream) => Unit) {
    val is = new FileInputStream(file)
    try {
      proccess(is)
    } finally {
      is.close
    }
  }

  /**
   * This method loads all information about subindexes and components
   */
  private def loadSubIndexInformation(is: InputStream) {
    val subIndexDao = new SubIndexDAOImpl(is)
    components = subIndexDao.getComponents
    subIndexes = subIndexDao.getSubIndexes
  }

  /**
   * This method loads all information abou indicators
   */
  private def loadIndicatorInformation(is: InputStream) {
    val indicatorDao = new IndicatorDAOImpl(is)
    primaryIndicators = indicatorDao.getPrimaryIndicators
    secondaryIndicators = indicatorDao.getSecondaryIndicators
    //Index all indicators in the reconciliator in order to search indicators
    indicatorReconciliator.indexIndicators(primaryIndicators)
    indicatorReconciliator.indexIndicators(secondaryIndicators)
  }

  /**
   * This method loads all information about countries
   */
  private def loadCountryInformation(uri: String, relativePath: Boolean) {
    val countryDao = new CountryDAOImpl(uri, relativePath)
    countries = countryDao.getCountries
  }

  /**
   * This method loads all information about regions
   */
  private def loadRegionInformation(is: InputStream) {
    val regionDao = new RegionDAOImpl(is)
    regions = regionDao.getRegions
  }

  private def loadProviderInformation(is: InputStream) {
    val providerDao = new ProviderDAOImpl(is)
    providers = providerDao.getProviders
  }

  //Obtain a country given it's name
  def obtainCountry(regionName: String): Country = {
    if (regionName == null || regionName.isEmpty()) {
      logger.error("The name of the country cannot " +
        "be null o empty")
      throw new IllegalArgumentException("The name of the country cannot " +
        "be null o empty")
    }
    var wiName: String = countryReconciliator.searchCountry(regionName)
    countries.find(c => c.name.equals(wiName)).getOrElse({
      logger.error("Not exist country with name " +
        regionName)
      throw new IllegalArgumentException("Not exist country with name " +
        regionName)
    })
  }

  //Obtain an indicator given it's name
  def obtainIndicator(indicatorName: String): Indicator = {
    val indicator = indicatorReconciliator.searchIndicator(indicatorName)
    if (indicator == null)
      logger.info("Not exist indicator with name " + indicatorName)
    indicator
  }

  //Obtain an indicator given it's id
  def obtainIndicatorById(id: String): Indicator = {
    val combined: ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.id.equals(id))
      .getOrElse(throw new IllegalArgumentException("Not exist indicator with " +
        "id " + id))
  }

  def obtainIndicatorByDescription(indicatorDescription: String): Indicator = {
    val combined: ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.comment.equalsIgnoreCase(indicatorDescription))
      .getOrElse(throw new IllegalArgumentException("Not exist indicator with " +
        "description " + indicatorDescription))
  }

  //Obtain a component given it's id
  def obtainComponent(componentId: String): Component = {
    components.find(component => component.id.equals(componentId)).getOrElse(throw new IllegalArgumentException("Not exist component " + componentId))
  }

  def getDatasets(): List[Dataset] = {
    return datasets
  }
  
  def getObservationsByStatus(status: ObservationStatus): List[Observation] = {
    val results: ListBuffer[Observation] = new ListBuffer[Observation]
    results.toList
    
  }

  def getComponentById(componentId: String): Component = {
    components.find(component => component.id.equals(componentId)).getOrElse(
      throw new IllegalArgumentException("There is no component with id " +
        componentId))
  }

}