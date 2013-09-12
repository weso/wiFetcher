package es.weso.wiFetcher.fetchers

import es.weso.wiFetcher.dao.CountryDAOImpl
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.IndicatorDAOImpl
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.dao.IndicatorDAO
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.dao.ObservationDAO
import es.weso.wiFetcher.dao.ObservationDAOImpl
import es.weso.wiFetcher.dao.CountryDAO
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.dao.DatasetDAO
import es.weso.wiFetcher.dao.DatasetDAOImpl
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.dao.SubIndexDAO
import es.weso.wiFetcher.dao.SubIndexDAOImpl
import es.weso.wiFetcher.entities.SubIndex
import es.weso.wiFetcher.entities.Component
import es.weso.wiFetcher.entities.ObservationStatus._
import es.weso.wiFetcher.dao.RegionDAO
import es.weso.wiFetcher.dao.RegionDAOImpl
import es.weso.wiFetcher.entities.Region
import es.weso.wiFetcher.analyzer.indicator.IndicatorReconciliator
import org.apache.log4j.Logger
import es.weso.wiFetcher.dao.ProviderDAOImpl
import es.weso.wiFetcher.dao.ProviderDAO
import es.weso.wiFetcher.entities.Provider
import java.io.InputStream
import java.io.File
import java.io.FileInputStream
import es.weso.reconciliator.CountryReconciliator
import es.weso.wiFetcher.utils.FileUtils

object SpreadsheetsFetcher extends Fetcher {

  private val logger: Logger = Logger.getLogger(this.getClass())

   var components: List[Component] = null
   var subIndexes: List[SubIndex] = null
   var primaryIndicators: List[Indicator] = null
   var secondaryIndicators: List[Indicator] = null
   var countries: List[Country] = null
  private val countryReconciliator : CountryReconciliator = 
    new CountryReconciliator(Configuration.getCountryReconciliatorFile, true)
   var regions: List[Region] = null
   var providers: List[Provider] = null
  //Create an indicator reconciliator
  val indicatorReconciliator: IndicatorReconciliator =
    new IndicatorReconciliator

  /**
   * This method load all structure about Web Index information
   */
  def loadStructure(f: File) {
    /*var is: InputStream = new FileInputStream(f)
    loadSubIndexInformation(is)
    is.close
    is = new FileInputStream(f)
    loadIndicatorInformation(is)
    is.close
    loadCountryInformation(Configuration.getCountryFile, true)
    is = new FileInputStream(f)
    loadRegionInformation(is)
    is.close
    is = new FileInputStream(f)
    loadProviderInformation(is)
    is.close*/
    handleFooIS(f, loadSubIndexInformation)
    handleFooIS(f, loadIndicatorInformation)
    loadCountryInformation(Configuration.getCountryFile, true)
    handleFooIS(f, loadRegionInformation)
    handleFooIS(f, loadProviderInformation)
  }

  def handleFooIS(file: File, foo:(InputStream)=>Unit) {
    val is = new FileInputStream(file)
    try {
      foo(is)
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
    if (regionName == null || regionName.isEmpty()){
      logger.error("The name of the country cannot " +
        "be null o empty")
      throw new IllegalArgumentException("The name of the country cannot " +
        "be null o empty")
    }
    var wiName : String = countryReconciliator.searchCountry(regionName)
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
    var combined: ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.id.equals(id))
      .getOrElse(throw new IllegalArgumentException("Not exist indicator with " +
        "id " + id))
  }

  def obtainIndicatorByDescription(indicatorDescription: String): Indicator = {
    var combined: ListBuffer[Indicator] = new ListBuffer
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