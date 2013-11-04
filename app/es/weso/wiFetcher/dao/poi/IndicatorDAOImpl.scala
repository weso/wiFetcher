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
import es.weso.wiFetcher.dao.IndicatorDAO
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.IndicatorHighLow
import es.weso.wiFetcher.entities.IndicatorType
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.POIUtils
import es.weso.wiFetcher.utils.IssueManagerUtils
import es.weso.wiFetcher.entities.Provider
import es.weso.wiFetcher.entities.traits.Component
import scala.collection.mutable.HashMap

/**
 * This class contains the implementation that allows to load all information
 * about Web Index indicators
 *
 * At the moment, this class extracts the information of the indicators from an
 * excel file that follows the structure of 2012 Web Index. Maybe we have
 * to change the implementation
 */
class IndicatorDAOImpl(is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher) extends IndicatorDAO
  with PoiDAO[Indicator] {

  import IndicatorDAOImpl._

  /**
   *  A list with all primary indicators of the Web Index
   */
  private val primaryIndicators: ListBuffer[Indicator] = ListBuffer.empty

  /**
   * A list with all secondary indicators of the Web Index
   */
  private val secondaryIndicators: ListBuffer[Indicator] = ListBuffer.empty

  load(is)

  /**
   * This method returns a list with all primary indicators
   */
  def getPrimaryIndicators(): List[Indicator] = {
    primaryIndicators.toList
  }

  /**
   * This method returns a list with all secondary indicators
   */
  def getSecondaryIndicators(): List[Indicator] = {
    secondaryIndicators.toList
  }

  protected def load(is: InputStream) {
    val workbook = WorkbookFactory.create(is)
    extractSheet(null, workbook) match {
      case Some(sheet) =>
        val indicators = parseData(workbook, sheet)
        for (indicator <- indicators) {
          indicator.indicatorType match {
            case IndicatorType.Primary =>
              primaryIndicators += indicator
            case IndicatorType.Secondary =>
              secondaryIndicators += indicator
            case IndicatorType.Wrong =>
          }
        }
      case _ =>
    }
  }

  private def extractSheet(path: Option[String], workbook: Workbook): Option[Sheet] = {
    //Obtain the sheet corresponding with indicators
    val sheet: Sheet = workbook.getSheet(SheetName)

    if (sheet == null) {
      sFetcher.issueManager.addError(
        message = s"The Indicators Sheet ${SheetName} does not exist",
        path = XslxFile)
      None
    } else Some(sheet)
  }

  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Indicator] = {

    //Obtain the initial cell to extract the information from properties files
    val cellReference = new CellReference(
      Configuration.getInitialCellIndicatorsSheet)
    //For each row, create a new indicator and extract all information
    logger.info("Begin indicators extraction")
    val indicators = for {
      row <- cellReference.getRow() to sheet.getLastRowNum()
      evaluator: FormulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator()
      actualRow = sheet.getRow(row)
      //In the properties file, we define the number of the columns that 
      //contains each indicator property
      id = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorIdColumn), evaluator)
      iType = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorTypeColumn), evaluator)
      name = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorNameColumn), evaluator)
      description = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorDescriptionColumn), evaluator)
      source = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorSourceColumn), evaluator)
      providerId = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorProviderColumn), evaluator)
      weight = POIUtils.extractNumericCellValue(actualRow.getCell(
        Configuration.getIndicatorWeightColumn), evaluator)
      hl = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorHLColumn), evaluator)
      componentId = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorComponentColumn), evaluator)
      frenchLabel = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorFrenchLabelColumn), evaluator)
      frenchComment = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorFrenchCommentColumn), evaluator)
      spanishLabel = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorSpanishLabelColumn), evaluator)
      spanishComment = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorSpanishCommentColumn), evaluator)
      arabicLabel = POIUtils.extractCellValue(actualRow.getCell(
        Configuration.getIndicatorArabicLabelColumn), evaluator)
      arabicComment = POIUtils.extractCellValue(actualRow.getCell(
          Configuration.getIndicatorArabicCommentColumn), evaluator)
      if(!id.isEmpty)
      component = sFetcher.obtainComponent(componentId, actualRow.getRowNum, 
          Configuration.getIndicatorComponentColumn) 
      providers = sFetcher.obtainProvider(providerId, actualRow.getRowNum,
        Configuration.getIndicatorProviderColumn)
      if(component.isDefined && !weight.isDefined/*&& !providers.isEmpty*/)
    } yield {
      val names : HashMap[String, String] = HashMap("en" -> name,
          "fr" -> frenchLabel,
          "es" -> spanishLabel,
          "ar" -> arabicLabel)
      val descriptions : HashMap[String, String] = HashMap("en" -> description,
          "fr" -> frenchComment,
          "es" -> spanishComment,
          "ar" -> arabicLabel)
      createIndicator(id, iType, names, descriptions, weight.get, hl, source,
        component.get, providers)
    }
    logger.info("Finish indicators extraction")
    indicators
  }

  /**
   * This method has to create an indicator from the parameters that receive
   * @param id The identifier of the indicator
   * @param subindex The subindex identifier that owns the indicator
   * @param component The component identifier that owns the indicator
   * @param name The complete name of the indicator
   * @param description A complete description of the indicator
   * @param source The source of the indicator
   * @param provider The entity or organization that provides indicator
   * information
   * @param typ The type of the indicator (primary or secondary)
   * @param weigth The weigth that has the indicator in order to calculate
   * the Web Index
   * @param hl A variable that indicates if for an indicator, values high or
   * low are preferred
   */
  def createIndicator(id: String, iType: String,
    names: HashMap[String, String], descriptions: HashMap[String, String], 
    weight: Double, hl: String, source: String, component: Component, 
    provider: ListBuffer[Provider]): Indicator = {
    val indicator = Indicator(
      id, iType match {
        case "Primary" => IndicatorType.Primary
        case "Secondary" => IndicatorType.Secondary
        case _ =>
          sFetcher.issueManager.addError(message = s"Indicator type '${iType}' is unknown",
            path = XslxFile, sheetName = Some(SheetName), cell = Some(iType))
          IndicatorType.Wrong
      },
      names, //lab,
      descriptions, //comme,
      null, null, 0, weight, hl match {
        case "High" => IndicatorHighLow.High
        case "Low" => IndicatorHighLow.Low
        case _ =>
          sFetcher.issueManager.addError(message = s"Indicator HighLow '${iType}' is unknown",
            path = XslxFile, sheetName = Some(SheetName), cell = Some(hl))
          IndicatorHighLow.Wrong
      },
      source, component, provider)
    component.addIndicator(indicator)
    indicator
  }

}

object IndicatorDAOImpl {

  /**
   * The name of the sheet that contains all indicators data
   */
  private val SheetName: String = "Indicators"

  private val XslxFile = Some("Structure File")

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

}