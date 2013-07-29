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

class SubIndexDAOImpl(path : String, relativePath : Boolean) extends SubIndexDAO{
  
  val SHEET_NAME = "Index"
  
  private var components : ListBuffer[Component] = new ListBuffer[Component]
  private var subIndexes : ListBuffer[SubIndex] = new ListBuffer[SubIndex]
  
  load(FileUtils.getFilePath(path, relativePath))
  
  def load(path : String) = {
    val workbook = WorkbookFactory.create(new FileInputStream(new File(path)))
    val sheet = workbook.getSheet(SHEET_NAME)
    if(sheet == null) 
      throw new IllegalArgumentException("Sheet " + SHEET_NAME + " does not " +
      		"exist in the file " + path)
    val initialCell = new CellReference(Configuration.getSubindexInitialCell)
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      val evaluator : FormulaEvaluator = 
        workbook.getCreationHelper().createFormulaEvaluator()
      val subindex = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexColumn), evaluator)
      val component = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getComponentColumn), evaluator)
  	  val weight = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexWeithColumn), 
          evaluator)
      val name = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexNameColumn), 
          evaluator)
      val description = POIUtils.extractCellValue(
          sheet.getRow(row).getCell(Configuration.getSubindexDescriptionColumn),
          evaluator)
      createEntity(subindex, component, weight, name, description) 
    }
  }
  
  def createEntity(subindex : String, component : String, weight : String, 
      name : String, description : String) = {
    if(subindex.equals(component))
      createSubIndex(subindex, weight, name, description)
    else {
      val comp = createComponent(component, weight, name, description)
      subIndexes.find(sub => sub.id.equals(subindex))
      .getOrElse(throw new IllegalArgumentException("Subindex not found"))
      .addComponent(comp)
    }
  }
  
  def createSubIndex(id : String, weight : String, name : String, 
      description : String) = {
    var subIndex = new SubIndex
    subIndex.id = id
    subIndex.name = name
    subIndex.description = description
    subIndex.weight = weight.toDouble
    subIndexes += subIndex
  }
  
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

  def getComponents() : List[Component] = {
    components.toSet.toList
  }
  
  def getSubIndexes() : List[SubIndex] = {
    subIndexes.toSet.toList
  }
}