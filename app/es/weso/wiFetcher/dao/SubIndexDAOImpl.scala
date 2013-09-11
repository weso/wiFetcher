package es.weso.wiFetcher.dao

import java.io.File
import java.io.FileInputStream
import scala.collection.mutable.ListBuffer
import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.WorkbookFactory
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.entities.Component
import es.weso.wiFetcher.entities.SubIndex
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.utils.POIUtils
import org.apache.log4j.Logger
import java.io.InputStream
import java.io.PushbackInputStream

/**
 * This class contains the implementation that allows to load all information 
 * about sub-indexes and components used by the Web Index
 * 
 * At the moment, the information is extracted from an excel file that follows
 * the structure of the 2012 Web Index. Maybe the implementation has to change
 */
class SubIndexDAOImpl(is : InputStream) extends SubIndexDAO{
  
  //The name of the sheet that contains the information
  val SHEET_NAME = "Index"
  
  //A list with all components loaded
  private var components : ListBuffer[Component] = new ListBuffer[Component]
  //A list with all sub-indexes loaded
  private var subIndexes : ListBuffer[SubIndex] = new ListBuffer[SubIndex]
  
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  load(is)
  
  /**
   * This method has to load the information about sub-indexes and components
   * @param path The path of the files that contains the information
   */
  private def load(is : InputStream) = {
    val workbook = WorkbookFactory.create(is)
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
    val initialCell = new CellReference(Configuration.getSubindexInitialCell)
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      logger.info("Begin sub-indexes and components extraction")
      val evaluator : FormulaEvaluator = 
        workbook.getCreationHelper().createFormulaEvaluator()
      //Extract the identifier of the sub-index
      val subindex = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexColumn), evaluator)
      //Extract the identifier of the component
      val component = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getComponentColumn), evaluator)
      //Extract the weight
  	  val weight = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexWeithColumn), 
          evaluator)
      //Extract the name
      val name = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexNameColumn), 
          evaluator)
      //Extract the description
      val description = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexDescriptionColumn),
          evaluator)
      //Create the entity (sub-index or component)
      createEntity(subindex, component, weight, name, description) 
    }
    logger.info("Finish extraction of sub-indexes and components")
  }
  
  /**
   * This method has to create a sub-index or component.
   * @param The identifier of a sub-index
   * @param The identifier of a component
   * @param The weight of the entity to calculate the Web Index
   * @param The name of the entity
   * @param The description of the entity
   */
  def createEntity(subindex : String, component : String, weight : String, 
      name : String, description : String) = {
    //We have to check if we have to create a sub-index or component. Following
    //the structure of the excel file, if the identifier of the sub-index and
    //the identifier of the component are equals, we have to create a sub-index.
    //Instead we have to create a component
    if(subindex.equals(component)) {
      logger.info("Create the sub-index " + subindex)
      createSubIndex(subindex, weight, name, description)
    } else {
      logger.info("Create the component " + component)
      //Create the component
      val comp = createComponent(component, weight, name, description)
      //Find the related sub-index, and add the component to it's components
      subIndexes.find(sub => sub.id.equals(subindex))
      .getOrElse(throw new IllegalArgumentException("Subindex not found"))
      .addComponent(comp)
    }
  }
  
  /**
   * This method has to create a sub-index
   * @param id The identifier of the sub-index
   * @param weight The weight that have the sub-index in order to calculate the
   * Web Index
   * @param name The name of the sub-index
   * @param description The description of the sub-index
   */
  def createSubIndex(id : String, weight : String, name : String, 
      description : String) = {
    var subIndex = new SubIndex
    subIndex.id = id
    subIndex.name = name
    subIndex.description = description
    subIndex.weight = weight.toDouble
    subIndexes += subIndex
  }
  
  /**
   * This method has to create a component
   * @param id The identifier of the component
   * @param Weight The weight that have the component in order to calculate
   * the Web Index
   * @param name The name of the component
   * @param description The description of the component
   */
  def createComponent(id : String, weight : String, name : String, 
      description : String) : Component = {
    var component = new Component
    component.id = id
    component.description = description
    component.name = name
    component.weight = weight.toDouble
    components += component
    component
  }

  /**
   * This method returns a list with all components
   */
  def getComponents() : List[Component] = {
    components.toSet.toList
  }
  
  /**
   * This method returns a list with all sub-indexes
   */
  def getSubIndexes() : List[SubIndex] = {
    subIndexes.toSet.toList
  }
}