package es.weso.wiFetcher.fetchers

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import scala.collection.mutable.ListBuffer
import org.apache.log4j.Logger
import es.weso.reconciliator.CountryReconciliator
import es.weso.wiFetcher.analyzer.indicator.IndicatorReconciliator
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.entity.DatasetDAOImpl
import es.weso.wiFetcher.dao.file.CountryDAOImpl
import es.weso.wiFetcher.dao.poi.IndicatorDAOImpl
import es.weso.wiFetcher.dao.poi.SecondaryObservationDAOImpl
import es.weso.wiFetcher.dao.poi.ProviderDAOImpl
import es.weso.wiFetcher.dao.poi.RegionDAOImpl
import es.weso.wiFetcher.dao.poi.SubIndexDAOImpl
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.issues._
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.ObservationStatus.ObservationStatus
import es.weso.wiFetcher.entities.Provider
import es.weso.wiFetcher.entities.Region
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.traits.SubIndex
import es.weso.wiFetcher.generator.ModelGenerator
import es.weso.wiFetcher.utils.IssueManagerUtils
import es.weso.wiFetcher.utils.FilterIssue
import es.weso.wiFetcher.dao.poi.PrimaryObservationDAOImpl
import es.weso.wiFetcher.generator.CSVGenerator

case class SpreadsheetsFetcher(structure: File, raw: File) extends Fetcher {

  import SpreadsheetsFetcher._

  private implicit val currentFetcher = this

  private val indicatorReconciliator = new IndicatorReconciliator
  val issueManager = new IssueManagerUtils()

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
  if(!issueManager.asSeq.isEmpty)
    issueManager.addWarn("There were problems parsing structure file, so ttl " +
    		"generated is not complete.", 
    		Some("Structure file"))
  loadObservations(raw)

  def issues: Seq[Issue] = {
    issueManager.addFilter(FilterIssue(col=Some(0),cell=Some("MEAN")))
    issueManager.addFilter(FilterIssue(col=Some(0),cell=Some("Mean")))
    issueManager.addFilter(FilterIssue(col=Some(0),cell=Some("SD")))
    issueManager.addFilter(FilterIssue(col=Some(0),cell=Some("s.d.")))
    issueManager.addFilter(FilterIssue(col=Some(0),cell=Some("OBSERVATIONS")))
    issueManager.addFilter(FilterIssue(col=Some(0),cell=Some("MEAN OF COUNTRIES WITH 5 YEARS DATA")))
    issueManager.filteredAsSeq
  }

  def storeAsTTL(baseUri: String, namespace: String, year : String/*, store: Boolean = false*/, timestamp : Long) =
    ModelGenerator(baseUri, namespace, year).generateJenaModel(this/*, store*/, timestamp)

  /**
   * This method load all structure about Web Index information
   */
  private def loadStructure(f: File) {
    safeLoadInformation(f, loadProviderInformation)
    safeLoadInformation(f, loadSubIndexInformation)
    safeLoadInformation(f, loadIndicatorInformation)
    loadDatasetInformation(secondaryIndicators.toList)
    loadCountryInformation(Configuration.getCountryFile, true)
    safeLoadInformation(f, loadRegionInformation)
  }
  
  def saveReport(timestamp : Long) : (Seq[Issue], String) = {
    val csvSchema = Array("Type", "Message", "Path", "sheetName", "Column", "Row", "Cell")
    val csvGenerator = CSVGenerator(csvSchema)
    val finalIssues = issues
    finalIssues.foreach(issue  => {
      val typ = issue match {
        case e: Error => "Error"
        case e: Warn => "Warning"
      }
      val value = Array(typ, issue.message, issue.path.getOrElse(""), issue.sheetName.getOrElse(""), issue.col.getOrElse("").toString, issue.row.getOrElse("").toString, issue.cell.getOrElse(""))
      csvGenerator.addValue(value)
    })
    val path = csvGenerator.save(timestamp)
    (finalIssues, path)
  }

  /**
   * This method load all observation form an excel file
   */
  private def loadObservations(f: File) {
    safeLoadInformation(f, loadSecondaryObservationInformation)
    safeLoadInformation(f, loadPrimaryObservationInformation)
  }

  private def loadDatasetInformation(indicators: List[Indicator]) {
    val datasetDao = new DatasetDAOImpl(indicators)
    datasets ++= datasetDao.getDatasets
  }

  private def loadPrimaryObservationInformation(is : InputStream) {
    val primaryObservationDao = new PrimaryObservationDAOImpl(is)
    observations ++= primaryObservationDao.getObservations
  }
 
  private def loadSecondaryObservationInformation(is: InputStream) {
    val secondaryObservationDao = new SecondaryObservationDAOImpl(is)
    observations ++= secondaryObservationDao.getObservations
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
    logger.info("Obtaining country with name: " + regionName)
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
  def obtainIndicator(indicatorName: String): Option[Indicator] = {
    val indicator = indicatorReconciliator.searchIndicator(indicatorName)
    if (indicator == null)
      logger.info(s"Not exist indicator with name ${indicatorName}")
    indicator
  }

  //Obtain an indicator given it's id
  def obtainIndicatorById(id: String): Option[Indicator] = {
    val combined: ListBuffer[Indicator] = ListBuffer.empty
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.id.equals(id))
  }

  def obtainIndicatorByDescription(indicatorDescription: String): Indicator = {
    val combined: ListBuffer[Indicator] = new ListBuffer
    combined.insertAll(0, primaryIndicators)
    combined.insertAll(0, secondaryIndicators)
    combined.find(indicator => indicator.comments
        .get("en").get.equalsIgnoreCase(indicatorDescription))
      .getOrElse(throw new IllegalArgumentException("Not exist indicator with "
        + s"description ${indicatorDescription}"))
  }

  //Obtain a component given it's id
  def obtainComponent(componentId: String, row : Int, col : Int): Option[Component] = {
    if(componentId.isEmpty()) {
      issueManager.addError("Component of a indicator cannot be empty", 
          Some("Structure file"), Some("Indicators"), Some(col), Some(row))
      None
    } else {      
	    val result = components.find(component => component.id.equals(componentId))
	    if(!result.isDefined)
	     issueManager.addError("Not exist component " + componentId, 
	          Some("Structure file"), Some("Indicators"), Some(col), Some(row))
	    result
	    }
  }
  
  def obtainProvider(providerId : String, row : Int, col : Int) : ListBuffer[Provider] = {
    val providersLocal : ListBuffer[Provider] = ListBuffer.empty    
    if(providerId.isEmpty()) {
      issueManager.addError("Provider of a indicator cannot be empty", 
          Some("Structure file"), Some("Indicators"), Some(col), Some(row))
    } else {
      
      val parts = providerId.split("/")
      parts.foreach(pvr =>{
        val result = providers.find(provider => provider.id.equals(pvr))
		if(!result.isDefined) {
		  val prov = obtainProviderByName(pvr, row, col)
		  if(prov.isDefined) 
		    providersLocal += prov.get
		  else
		    issueManager.addError("Not exist provider " + pvr, 
			  Some("Structure file"), Some("Indicators"), Some(col), Some(row))  
		} else {
		  providersLocal += result.get
		}
		
      })
    }
    providersLocal
  }
  
  def obtainProviderByName(providerName : String, row : Int, col : Int) : Option[Provider] = {
      val result = providers.find(provider => provider.name.equalsIgnoreCase(providerName.trim))
	  result
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
}

object SpreadsheetsFetcher {

  private val countryReconciliator =
    new CountryReconciliator(Configuration.getCountryReconciliatorFile, true)

  private val logger: Logger = Logger.getLogger(this.getClass())

}