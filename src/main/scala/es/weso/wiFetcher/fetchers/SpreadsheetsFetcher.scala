package es.weso.wiFetcher.fetchers

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.utils.POIUtils

class SpreadsheetsFetcher extends Fetcher {
	 
  var file : InputStream = null
  var workbook : Workbook = null
  
  def loadWorkbook(path : String, relativePath : Boolean = false) = {
    val currentFile = if(relativePath)
      new File(getClass().getClassLoader().getResource(path).getPath())
    else
      new File(path)
    file = new FileInputStream(currentFile)
    workbook = WorkbookFactory.create(file)
  }
  
  def getObservations(datasets : List[Dataset]) : List[Observation] = {
    var observations = List[Observation]()
    for(dataset <- datasets)
      observations:::extractObservationsByDataset(dataset)
    observations
  }
  
  def extractObservationsByDataset(dataset: Dataset) : List[Observation] = {
    var observations = List[Observation]()
    val sheet = workbook.getSheet(dataset.id)
    if(sheet == null) {
      throw new IllegalArgumentException("There isn't data for dataset: " + dataset.id)
    }
    val initialCell = new CellReference(
        Configuration.getInitialCellSecondaryObservation)
    for(row <- initialCell.getRow() to sheet.getLastRowNum()) {
      val actualRow = sheet.getRow(row)
      if(actualRow != null) {
        var country : String = POIUtils.extractCellValue(actualRow.getCell(0))
        for(column <- initialCell.getCol() to actualRow.getLastCellNum() - 1) {
        	var year = POIUtils.extractCellValue(sheet.getRow(
        	    initialCell.getRow() - 2).getCell(column))
    	    var value = POIUtils.extractCellValue(actualRow.getCell(column))
        	println("Country: " + country + " year: " + year + " value: " + 
        	    value)
        }
      }
    }
    observations
  } 
  
}