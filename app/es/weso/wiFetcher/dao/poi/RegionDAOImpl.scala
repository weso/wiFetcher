package es.weso.wiFetcher.dao.poi

import java.io.InputStream
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.RegionDAO
import es.weso.wiFetcher.entities.Region
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.IssueManagerUtils
import es.weso.wiFetcher.utils.POIUtils
import org.apache.poi.ss.usermodel.FormulaEvaluator

/**
 * This class contains the implementation that allows to load all information
 * about the regions that are used by the Web Index.
 *
 * At the moment, this class extracts the information of the regions from an
 * excel file that follows the structure of 2012 Web Index. Maybe we have to
 * change the implementation
 */
class RegionDAOImpl(is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends RegionDAO with PoiDAO[Region] {

  import RegionDAOImpl._

  //A list with all regions  
  private val regions: ListBuffer[Region] = ListBuffer.empty

  load(is)

  /**
   * This method has to extract the information from an excel file. This file
   * contains the name of the regions and the countries that are part of them.
   * @param path the path of the file that contains the information
   * @return A list with all regions loaded
   */
  protected def load(is: InputStream) {
    val workbook = WorkbookFactory.create(is)
    //Obtain corresponding sheet
    val sheet: Sheet = workbook.getSheet(SheetName)

    if (sheet == null) {
      sFetcher.issueManager.addError(
        message = new StringBuilder("The Regions Sheet ").append(SheetName)
          .append(" does not exist").toString, path = XslxFile, sheetName = Some(SheetName))
    } else {
      logger.info("Begin region extraction")
      regions ++= parseData(workbook, sheet)
      logger.info("Finish region extraction")
    }
  }

  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Region] = {
    val sheet: Sheet = workbook.getSheet(SheetName)
    val cellReference = new CellReference(Configuration.getRegionInitialCell)
    logger.info("Begin extraction of region information")

    val evaluator = workbook.getCreationHelper().createFormulaEvaluator()
    val regions = loadRegions(sheet, cellReference, evaluator)
    for {
      row <- cellReference.getRow() to sheet.getLastRowNum()
      //Extract the name of the region. The column that contains the information
      //is in the properties file
      regionName = POIUtils.extractCellValue(sheet.getRow(row).getCell(
        Configuration.getRegionNameColumn), evaluator)

      region = regions(regionName)

      //Extract the country name. The column that contains the name of the 
      //country in in the properties file
      countryName = POIUtils.extractCellValue(sheet.getRow(row).getCell(
        Configuration.getRegionCountryColumn), evaluator)
    } {
      sFetcher.obtainCountry(countryName) match {
        case Some(country) => region.addCountry(country)
        case None => None
      }
    }
    regions.values.toList
  }

  /**
   * Returns a list of regions
   */
  def getRegions(): List[Region] = {
    regions.toList
  }

  protected def loadRegions(sheet: Sheet, cellReference: CellReference,
    evaluator: FormulaEvaluator): Map[String, Region] = {
    (for {
      row <- cellReference.getRow() to sheet.getLastRowNum()
      //Extract the name of the region. The column that contains the information
      //is in the properties file
      regionName = POIUtils.extractCellValue(sheet.getRow(row).getCell(
        Configuration.getRegionNameColumn), evaluator)
    } yield {
      regionName -> Region(regionName)
    }).toMap
  }

}

object RegionDAOImpl {

  /**
   * The name of the sheet that contains the information about regions
   */
  private val SheetName = "Countries"

  private val XslxFile = Some("Structure File")

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
}