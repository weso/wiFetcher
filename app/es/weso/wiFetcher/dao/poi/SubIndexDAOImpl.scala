package es.weso.wiFetcher.dao.poi

import java.io.InputStream

import scala.collection.mutable.ListBuffer

import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.dao.SubIndexDAO
import es.weso.wiFetcher.entities.Entity
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.traits.SubIndex
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.utils.IssueManagerUtils
import es.weso.wiFetcher.utils.POIUtils

/**
 * This class contains the implementation that allows to load all information
 * about sub-indexes and components used by the Web Index
 *
 * At the moment, the information is extracted from an excel file that follows
 * the structure of the 2012 Web Index. Maybe the implementation has to change
 */
class SubIndexDAOImpl(is: InputStream)(implicit val sFetcher: SpreadsheetsFetcher)
  extends SubIndexDAO with PoiDAO[Entity] {

  import SubIndexDAOImpl._

  //A list with all components loaded
  private val components: ListBuffer[Component] = ListBuffer.empty
  //A list with all sub-indexes loaded
  private val subIndexes: ListBuffer[SubIndex] = ListBuffer.empty

  load(is)

  /**
   * This method has to load the information about sub-indexes and components
   * @param path The path of the files that contains the information
   */
  protected def load(is: InputStream) {
    logger.info("Starting extraction of sub-indexes and components")
    val workbook = WorkbookFactory.create(is)
    //Obtain the corresponding sheet
    val sheet = workbook.getSheet(SheetName)
    if (sheet == null) {
      sFetcher.issueManager.addError(
        message = new StringBuilder("The Subindex Sheet ").append(SheetName)
        .append(" does not exist").toString, path = XslxFile)
    } else {
      val entities = parseData(workbook, sheet)
      enchainEntities(entities)
      logger.info("Finish extraction of sub-indexes and components")
    }
  }

  /**
   * Load all the entities, these components are sorted by reading appearance
   * @param
   * @param
   */
  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[Entity] = {
    val initialCell = new CellReference(Configuration.getSubindexInitialCell)
    for {
      row <- initialCell.getRow() to sheet.getLastRowNum()
      evaluator = workbook.getCreationHelper().createFormulaEvaluator()
      actualRow = sheet.getRow(row)
      //Extract the identifier of the sub-index
      eType = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getTypeColumn), evaluator)
      //Extract the identifier of the component
      id = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getIdColumn), evaluator)
      //Extract the name
      name = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getSubindexNameColumn), evaluator)
      //Extract the description
      description = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getSubindexDescriptionColumn), evaluator)
      //Extract the weight
      weight = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getSubindexWeithColumn), evaluator)
      //Create the entity (sub-index or component)
    } yield {
      createEntity(eType, id, name, description, weight)
    }
  }

  /**
   * @param entities
   */
  def enchainEntities(entities: Seq[Entity]) {
    /**
     * @param subIndex
     * @param entities
     */
    def inner(subIdex: SubIndex, entities: Seq[Entity]) {
      entities match {
        case head :: tail => head match {
          case s: SubIndex =>
            inner(s, tail)
          case c: Component =>
            subIdex.addComponent(c)
            inner(subIdex, tail)
          case _ =>
        }
        case _ =>
      }
    }
    entities.head match {
      case e: SubIndex => inner(e, entities.tail)
      case _ =>
        sFetcher.issueManager.addError(message = s"The head element is not a SubIndex",
          path = XslxFile)
        List.empty
    }
  }
  /**
   * This method has to create a SubIndex or a Component.
   * @param The entity's type
   * @param The id of the entity (Shortened version of the name)
   * @param The weight of the entity to calculate the Web Index
   * @param The name of the entity
   * @param The description of the entity
   */
  def createEntity(eType: String, id: String, weight: String,
    name: String, description: String): Entity = {
    eType match {
      case e if (e == SubindexType) =>
        createSubIndex(id, weight, name, description)
      case e if (e == ComponentType) =>
        createComponent(id, weight, name, description)
      case _ =>
        sFetcher.issueManager.addError(message = new StringBuilder("Unknown type '")
        .append(eType).append(" in Structure Sheet").toString, path = XslxFile,
          sheetName = Some(SheetName), cell = Some(eType))
        createWrongEntity
    }
  }

  /**
   * This method has to create a sub-index
   * @param id The identifier of the sub-index
   * @param name The name of the sub-index
   * @param description The description of the sub-index
   * @param weight The weight that have the sub-index in order to calculate the
   * Web Index
   */
  def createSubIndex(id: String, name: String, description: String,
    weight: String): SubIndex = {
    logger.info("Create the component: {}" + { id })
    val subIndex = new Entity(id, name, description, weight.toDouble) with SubIndex
    subIndexes += subIndex
    subIndex
  }

  /**
   * This method has to create a component
   * @param id The identifier of the component
   * @param name The name of the component
   * @param description The description of the component
   * @param Weight The weight that have the component in order to calculate
   * the Web Index
   */
  def createComponent(id: String, name: String, description: String,
    weight: String): Component = {
    logger.info("Create the sub-index: {}", id)
    val component = new Entity(id, name, description, weight.toDouble) with Component
    components += component
    component
  }

  def createWrongEntity = Entity("wrong", "wrong", "wrong", 0)

  /**
   * This method returns a list with all components
   */
  def getComponents: List[Component] = {
    components.toSet.toList
  }

  /**
   * This method returns a list with all sub-indexes
   */
  def getSubIndexes: List[SubIndex] = {
    subIndexes.toSet.toList
  }

}

object SubIndexDAOImpl {

  /**
   * The name of the sheet that contains the information
   */
  private val SheetName = "Structure"
  private val XslxFile = Some("Structure File")

  private val SubindexType = "subindex"
  private val ComponentType = "component"

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
}