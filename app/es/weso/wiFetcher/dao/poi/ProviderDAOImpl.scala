package es.weso.wiFetcher.dao.poi

import java.io.InputStream
import scala.collection.mutable.ListBuffer
import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.ProviderDAO
import es.weso.wiFetcher.entities.Provider
import es.weso.wiFetcher.utils.POIUtils
import es.weso.wiFetcher.utils.IssueManagerUtils
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

/**
 * This class contains the implementation that allows to load all information
 * about providers used by the Web Index
 *
 * At the moment, the information is extracted from an excel file that follows
 * the structure of the 2012 Web Index. Maybe the implementation has to change
 */
class ProviderDAOImpl(is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends ProviderDAO with PoiDAO[Provider] {

  import ProviderDAOImpl._

  private val providers: ListBuffer[Provider] = ListBuffer.empty

  load(is)

  /**
   * This method has to load the information about providers
   * @param path The path of the files that contains the information
   */
  protected def load(is: InputStream) {
    val workbook = WorkbookFactory.create(is)
    //Obtain the corresponding sheet
    val sheet = workbook.getSheet(SheetName)

    if (sheet == null) {
      sFetcher.issueManager.addError(
        message = s"The Indicators Sheet ${SheetName} does not exist",
        path = XslxFile)
    } else {
      logger.info("Begin providers extraction")
      providers ++= parseData(workbook, sheet)
      logger.info("Finish extraction of providers")
    }
  }

  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Provider] = {
    //Obtain the first cell to load data. The first cell is in the properties 
    //file
    val initialCell = new CellReference(Configuration.getProvierInitialCell)
    for {
      row <- initialCell.getRow() to sheet.getLastRowNum()
      evaluator: FormulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator()
      //Extract the identifier of the provider
      id = POIUtils.extractCellValue(
        sheet.getRow(row).getCell(Configuration.getProviderIdColumn), evaluator)
      //Extract the name of the provider
      name = POIUtils.extractCellValue(
        sheet.getRow(row).getCell(Configuration.getProviderNameColumn), evaluator)
      //Extract the web of the provider
      web = POIUtils.extractCellValue(
        sheet.getRow(row).getCell(Configuration.getProviderWebColumn),
        evaluator)
      //Extract the source of the provider
      source = POIUtils.extractCellValue(
        sheet.getRow(row).getCell(Configuration.getProviderSourceColumn),
        evaluator)
    } yield {
      Provider(id, name, web, source)
    }
  }

  def getProviders(): List[Provider] = {
    providers.toList
  }

}

object ProviderDAOImpl {

  /**
   * The name of the sheet that contains the information
   */
  private val SheetName = "Providers"

  private val XslxFile = Some("Structure File")

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
}