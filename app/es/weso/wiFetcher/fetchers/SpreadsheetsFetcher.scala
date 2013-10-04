package es.weso.wiFetcher.fetchers

import scala.collection.mutable.ListBuffer
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import scala.collection.mutable.ListBuffer
import org.apache.log4j.Logger
import es.weso.wiFetcher.analyzer.indicator.IndicatorReconciliator
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.file.CountryDAOImpl
import es.weso.wiFetcher.dao.entity.DatasetDAOImpl
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
import es.weso.wiFetcher.utils.IssueManagerUtils
import com.hp.hpl.jena.rdf.model.Model
import es.weso.wiFetcher.generator.ModelGenerator
import es.weso.reconciliator.CountryReconciliator
import es.weso.wiFetcher.entities.issues.Issue

case class SpreadsheetsFetcher(structure: File, raw: File) extends Fetcher {
  
  import SpreadsheetsFetcher._

  private implicit val currentFetcher = this
  
  private val indicatorReconciliator = new IndicatorReconciliator
  
  val components: ListBuffer[Component] = ListBuffer.empty
  val subIndexes: ListBuffer[SubIndex] = ListBuffer.empty
  val primaryIndicators: ListBuffer[Indicator] = ListBuffer.empty
  val secondaryIndicators: ListBuffer[Indicator] = ListBuffer.empty
  val countries: ListBuffer[Country] = ListBuffer.empty
  val regions: ListBuffer[Region] = ListBuffer.empty
  val providers: ListBuffer[Provider] = ListBuffer.empty
  val datasets: ListBuffer[Dataset] = ListBuffer.empty
  val observations: ListBuffer[Observation] = ListBuffer.empty

  loadStructure(structure)
  loadObservations(raw)

  def issues: Seq[Issue] = IssueManagerUtils.asSeq
    
  def storeAsTTL(store:Boolean=false) = 
    ModelGenerator.generateJenaModel(this,store)

  /**
   * This method load all structure about Web Index information
   */
  private def loadStructure(f: File) {
    safeLoadInformation(f, loadSubIndexInformation)
    safeLoadInformation(f, loadIndicatorInformation)
    loadDatasetInformation(secondaryIndicators.toList)
    loadCountryInformation(Configuration.getCountryFile, true)
    safeLoadInformation(f, loadRegionInformation)
    safeLoadInformation(f, loadProviderInformation)
  }

  /**
   * This method load all observation form an excel file
   */
  private def loadObservations(f: File) {
    safeLoadInformation(f, loadObservationInformation)
  }

  private def loadDatasetInformation(indicators: List[Indicator]) {
    val datasetDao = new DatasetDAOImpl(indicators)
    datasets ++= datasetDao.getDatasets
  }

  private def loadObservationInformation(is: InputStream) {
    val observationDao = new ObservationDAOImpl(is)
    observations ++= observationDao.getObservations
  }

  private def safeLoadInformation(file: File, proccess: (InputStream) => Unit) {
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
    components ++= subIndexDao.getComponents
    subIndexes ++= subIndexDao.getSubIndexes
  }

  /**
   * This method loads all information abou indicators
   */
  private def loadIndicatorInformation(is: InputStream) {
    val indicatorDao = new IndicatorDAOImpl(is)
    primaryIndicators ++= indicatorDao.getPrimaryIndicators
    secondaryIndicators ++= indicatorDao.getSecondaryIndicators
    //Index all indicators in the reconciliator in order to search indicators
    indicatorReconciliator.indexIndicators(primaryIndicators.toList)
    indicatorReconciliator.indexIndicators(secondaryIndicators.toList)
  }

  /**
   * This method loads all information about countries
   */
  private def loadCountryInformation(uri: String, relativePath: Boolean) {
    val countryDao = new CountryDAOImpl(uri, relativePath)
    countries ++= countryDao.getCountries
  }

  /**
   * This method loads all information about regions
   */
  private def loadRegionInformation(is: InputStream) {
    val regionDao = new RegionDAOImpl(is)
    regions ++= regionDao.getRegions
  }

  private def loadProviderInformation(is: InputStream) {
    val providerDao = new ProviderDAOImpl(is)
    providers ++= providerDao.getProviders
  }

  //Obtain a country given it's name
  def obtainCountry(regionName: String): Option[Country] = {
    if (regionName == null || regionName.isEmpty) {
      logger.error("The name of the country cannot be null o empty")
      throw new IllegalArgumentException("The name of the country cannot " +
        "be null o empty")
    }
    countryReconciliator.searchCountry(regionName) match {
      case Some(name) => countries.find(c => c.name.equals(name))
      case None => None
    }
  }

  //Obtain an indicator given it's name
  def obtainIndicator(indicatorName: String): Indicator = {
    val indicator = indicatorReconciliator.searchIndicator(indicatorName)
    if (indicator == null)
      logger.info(s"Not exist indicator with name ${indicatorName}")
    indicator
  }

  //Obtain an indicator given it's id
  def obtainIndicatorById(id: String): Indicator = {
    val combined: ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.id.equals(id))
      .getOrElse(throw new IllegalArgumentException("Not exist indicator with " +
        s"id ${id}"))
  }

  def obtainIndicatorByDescription(indicatorDescription: String): Indicator = {
    val combined: ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.comment.equalsIgnoreCase(indicatorDescription))
      .getOrElse(throw new IllegalArgumentException("Not exist indicator with "
        + s"description ${indicatorDescription}"))
  }

  //Obtain a component given it's id
  def obtainComponent(componentId: String): Component = {
    components.find(component => component.id.equals(componentId))
      .getOrElse(throw new IllegalArgumentException("Not exist component " + componentId))
  }

  def getDatasets(): List[Dataset] = {
    return datasets.toList
  }

  def getDatasetById(id: String): Dataset = {
    datasets.filter(_.id == id).head
  }

  def getObservationsByStatus(status: ObservationStatus): List[Observation] = {
    observations.filter(_.status == status).toList
  }

  def getComponentById(componentId: String): Component = {
    components.find(component => component.id.equals(componentId)).getOrElse(
      throw new IllegalArgumentException("There is no component with id " +
        componentId))
  }
}

object SpreadsheetsFetcher {

  private val countryReconciliator =
    new CountryReconciliator(Configuration.getCountryReconciliatorFile, true)

  private val logger: Logger = Logger.getLogger(this.getClass())

}