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

object SpreadsheetsFetcher extends Fetcher {
  
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  //Creates CountryDAO to load all countries information
  private val countryDao : CountryDAO = new CountryDAOImpl(
      Configuration.getCountryFile, true)
  //Creates SubIndexDao to load all subindexes and components information
  private val subindexDao : SubIndexDAO = new SubIndexDAOImpl(
      Configuration.getSubindexFile, true)
  //Load all subindexes information
  val subindexes : List[SubIndex] = subindexDao.getSubIndexes
  //Load all components information
  val components : List[Component] = subindexDao.getComponents
  //Creates IndicatorDAO in order to load all indicators information
  private val indicatorDao : IndicatorDAO =  new IndicatorDAOImpl(
      Configuration.getIndicatorFilename, true)
  //Creates ObservationDAO in order to load all observations information
  private val observationDao : ObservationDAO = new ObservationDAOImpl(
      Configuration.getObservationFile, true) 
  //Creates DatasetDAO in order to load all datasets information
  private val datasetDao : DatasetDAO = new DatasetDAOImpl(
      Configuration.getDatasetFile, true)
  //Obtain all datasets
  val datasets : List[Dataset] = datasetDao.getDatasets
  //Obtain all countries
  val countries : List[Country] = countryDao.getCountries
  //Obtain all primary indicators    
  val primaryIndicators : List[Indicator] = indicatorDao.getPrimaryIndicators
  //Obtain all secondary indicators
  val secondaryIndicators : List[Indicator] = 
    indicatorDao.getSecondaryIndicators
  //Create an indicator reconciliator
  val indicatorReconciliator : IndicatorReconciliator = 
    new IndicatorReconciliator
  //Index all indicators in the reconciliator in order to search indicators
  indicatorReconciliator.indexIndicators(primaryIndicators)
  indicatorReconciliator.indexIndicators(secondaryIndicators)
  //Obtain all observations  
  val observations : List[Observation] = observationDao.getObservations(datasets)
  //Creates RegionDAO in order to load all regions information
  private val regionDao : RegionDAO = new RegionDAOImpl(
      Configuration.getRegionsFilename, true)  
  //Obtain all regions
  val regions : List[Region] = regionDao.getRegions
  
  val observationsByDataset : Map[Dataset, List[Observation]] = observations.groupBy(observation => observation.dataset)
  val observationsByYear : Map[Int, List[Observation]] = observations.groupBy(observation => observation.year)
  val observationsByCountry : Map[String, List[Observation]] = observations.groupBy(observation => observation.area.iso3Code)
  
  //Obtain a country given it's name
  def obtainCountry(regionName : String) : Country = {
    if(regionName == null || regionName.isEmpty()) 
      throw new IllegalArgumentException("The name of the country cannot " +
      		"be null o empty")
    countries.find(c => c.name.equals(regionName)).getOrElse(
        throw new IllegalArgumentException("Not exist country with name " + 
            regionName))
  }
  
  //Obtain an indicator given it's name
  def obtainIndicator(indicatorName : String) : Indicator = {
    val indicator = indicatorReconciliator.searchIndicator(indicatorName)
    if(indicator == null)
      logger.info("Not exist indicator with name " + indicatorName)
    indicator
  }
  
  //Obtain an indicator given it's id
  def obtainIndicatorById(id : String) : Indicator = {
    var combined : ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.id.equals(id))
    .getOrElse(throw new IllegalArgumentException("Not exist indicator with " +
    		"id " + id))
  }
  
  def obtainIndicatorByDescription(indicatorDescription : String) : Indicator = {
     var combined : ListBuffer[Indicator] = new ListBuffer
     combined.insertAll(0, primaryIndicators)
     combined.insertAll(0, secondaryIndicators)
     combined.find(indicator => indicator.comment.equalsIgnoreCase(indicatorDescription))
    .getOrElse(throw new IllegalArgumentException("Not exist indicator with " +
    		"description " + indicatorDescription))
  }
  
  //Obtain a component given it's id
  def obtainComponent(componentId : String) : Component = {
    components.find(component => component.id.equals(componentId)).getOrElse(throw new IllegalArgumentException("Not exist component " + componentId))
  }
  
  def getObservationsByStatus(status : ObservationStatus) : List[Observation] = {
    val results : ListBuffer[Observation] = new ListBuffer[Observation]
    results.toList
  }
  
  def getComponentById(componentId : String) : Component = {
    components.find(component => component.id.equals(componentId)).getOrElse(
        throw new IllegalArgumentException("There is no component with id " + 
            componentId))
  }
  
}