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
import scala.collection.mutable.HashMap
import es.weso.wiFetcher.entities.traits.Index

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
  
  private val indexes : ListBuffer[Index] = ListBuffer.empty
  
  
  load(is)

  /**
   * This method has to load the information about sub-indexes and components
   * @param path The path of the files that contains the information
   */
  protected def load(is: InputStream) = {
    logger.info("Starting extraction of sub-indexes and components")
    val workbook = WorkbookFactory.create(is)
    //Obtain the corresponding sheet
    val sheet = workbook.getSheet(SheetName)
    if (sheet == null) {
      sFetcher.issueManager.addError(
        message = new StringBuilder("The Subindex Sheet ").append(SheetName)
          .append(" does not exist").toString, sheetName = Some(SheetName), path = XslxFile)
          None
    } else {
      val entities = parseData(workbook, sheet)
      enchainEntities(entities)
      indexes.head.addSubindexes(subIndexes)
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
      order = POIUtils.extractNumericCellValue(
          actualRow.getCell(Configuration.getSubindexOrderColumn), 
          evaluator)
      color = POIUtils.extractCellValue(
          actualRow.getCell(Configuration.getSubindexColorColumn), evaluator)
      //Extract the description
      description = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getSubindexDescriptionColumn), evaluator)
      //Extract the weight
      weight = POIUtils.extractNumericCellValue(
        actualRow.getCell(Configuration.getSubindexWeithColumn), evaluator)
      frenchLabel = POIUtils.extractCellValue(
        actualRow.getCell(Configuration.getSubindexFrenchLabelColumn), evaluator)
      frenchComment = POIUtils.extractCellValue(
          actualRow.getCell(Configuration.getSubindexFrenchCommentColumn), evaluator)
      spanishLabel = POIUtils.extractCellValue(
          actualRow.getCell(Configuration.getSubindexSpanishLabelColumn), evaluator)
      spanishComment = POIUtils.extractCellValue(
          actualRow.getCell(Configuration.getSubindexSpanishCommentColumn), evaluator)
      arabicLabel = POIUtils.extractCellValue(
          actualRow.getCell(Configuration.getSubindexArabicLabelColumn), evaluator)
      arabicComment = POIUtils.extractCellValue(
          actualRow.getCell(Configuration.getSubindexArabicCommentColumn), evaluator)
      //Create the entity (sub-index or component)
      if(weight.isDefined)
    } yield {
      val names : HashMap[String, String] = HashMap("en" -> name,
          "fr" -> frenchLabel,
          "es" -> spanishLabel,
          "ar" -> arabicLabel)
      val comments : HashMap[String, String] = HashMap("en" -> description,
          "fr" -> frenchComment,
          "es" -> spanishComment,
          "ar" -> arabicComment)
      createEntity(eType, id, weight.get, names, comments, order.get.toInt, color)
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
    def inner(subIndex: SubIndex, entities: Seq[Entity]) {
      if(!entities.isEmpty) {
        val head = entities.head
		head match {
			case s: SubIndex =>
            	inner(s, entities.tail)
			case c: Component =>
            	subIndex.addComponent(c)
            	inner(subIndex, entities.tail)
			case _ =>
        }
      }
    }
    entities.drop(1).head match {
      case e: SubIndex =>
        inner(e, entities.tail)
      case _ =>
        sFetcher.issueManager.addError(
          message = s"The head element is not a SubIndex",
          sheetName = Some(SheetName), path = XslxFile)
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
  def createEntity(eType: String, id: String, weight: Double,
    names: HashMap[String, String], 
    descriptions: HashMap[String, String], order : Int, color : String): Entity = {
    eType match {
      case e if (e == SubindexType) =>
        createSubIndex(id, names, descriptions, order, color, weight)
      case e if (e == ComponentType) =>
        createComponent(id, names, descriptions, order, color, weight)
      case e if (e == IndexType) => 
        createIndex(id, names, descriptions, order, color, weight)
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
  def createSubIndex(id: String, names: HashMap[String, String], 
      descriptions: HashMap[String, String], order : Int, color : String, 
      weight: Double): SubIndex = {
    logger.info("Create the sub-index: {}" + { id })
    val subIndex = new Entity(id, names, descriptions, order, color, weight) with SubIndex
    subIndexes += subIndex
    subIndex
  }
  
  /**
   * This method has to create the index
   * @param id The identifier of the the index
   * @param name The name of the index
   * @param description The description of the index
   * @param weight The weight that have the index in order to calculate the
   * Web Index
   */
  def createIndex(id: String, names: HashMap[String, String], 
      descriptions: HashMap[String, String], order : Int, color : String, 
      weight: Double): Index = {
    logger.info("Create the index: {}" + { id })
    val index = new Entity(id, names, descriptions, order, color, weight) with Index
    indexes += index
    index
  }

  /**
   * This method has to create a component
   * @param id The identifier of the component
   * @param name The name of the component
   * @param description The description of the component
   * @param Weight The weight that have the component in order to calculate
   * the Web Index
   */
  def createComponent(id: String, names: HashMap[String, String], 
      descriptions: HashMap[String, String], order : Int, color : String, 
      weight: Double): Component = {
    logger.info("Create the component: {}", id)
    val component = new Entity(id, names, descriptions, order, color, weight) with Component
    components += component
    component
  }

  def createWrongEntity = Entity("wrong", HashMap("en" -> "wrong"), 
      HashMap("en"->"wrong"), 0)

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
  
  def getIndexes : ListBuffer[Index] = {
    indexes
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
  private val IndexType = "index"

  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
}