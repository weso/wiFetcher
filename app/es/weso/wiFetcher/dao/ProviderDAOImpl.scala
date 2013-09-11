package es.weso.wiFetcher.dao

import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.entities.Provider
import org.apache.log4j.Logger
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.hssf.util.CellReference
import java.io.FileInputStream
import es.weso.wiFetcher.utils.POIUtils
import org.apache.poi.ss.usermodel.FormulaEvaluator
import es.weso.wiFetcher.configuration.Configuration
import java.io.File
import java.io.InputStream
import java.io.PushbackInputStream

/**
 * This class contains the implementation that allows to load all information 
 * about providers used by the Web Index
 * 
 * At the moment, the information is extracted from an excel file that follows
 * the structure of the 2012 Web Index. Maybe the implementation has to change
 */
class ProviderDAOImpl(is : InputStream) 
	extends ProviderDAO{
  
  val SHEET_NAME = "Providers"
    
  private var providers : ListBuffer[Provider] = new ListBuffer[Provider]
  
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  load(is)
  
  /**
   * This method has to load the information about providers
   * @param path The path of the files that contains the information
   */
  private def load(is : InputStream) = {
    var input = is
    if(!input.markSupported())
      input = new PushbackInputStream(is, 8)
    val workbook = WorkbookFactory.create(input)
    //Obtain the corresponding sheet
    val sheet = workbook.getSheet(SHEET_NAME)
    if(sheet == null) {
      logger.error("Sheet " + SHEET_NAME + " does not " +
      		"exist in the file specified")
      throw new IllegalArgumentException("Sheet " + SHEET_NAME + " does not " +
      		"exist in the file specified")
    }
    //Obtain the first cell to load data. The first cell is in the properties 
    //file
    val initialCell = new CellReference(Configuration.getProvierInitialCell)
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      logger.info("Begin providers extraction")
      val evaluator : FormulaEvaluator = 
        workbook.getCreationHelper().createFormulaEvaluator()
      //Extract the identifier of the provider
      val id = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getProviderIdColumn), evaluator)
      //Extract the name of the provider
      val name = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getProviderNameColumn), evaluator)
      //Extract the web of the provider
  	  val web = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getProviderWebColumn), 
          evaluator)
      //Extract the source of the provider
      val source = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getProviderSourceColumn), 
          evaluator)
      //Create the provider
      val provider = new Provider
      provider.id = id
      provider.name = name
      provider.web = web
      provider.source = source
      
      providers += provider
    }
    logger.info("Finish extraction of sub-indexes and components")
  }
  
  def getProviders() : List[Provider] = {
    providers.toList
  }

}