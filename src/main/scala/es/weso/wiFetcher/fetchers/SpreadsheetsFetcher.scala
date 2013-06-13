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
import scala.collection.mutable.ListBuffer

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
    if(datasets == null)
      throw new IllegalArgumentException("List of datasets to load their " +
      		"observations is null")
    var observations = ListBuffer[Observation]()
    for(dataset <- datasets)
      observations.insertAll(0, extractObservationsByDataset(dataset))
    observations.toList
  }
  
  def extractObservationsByDataset(dataset: Dataset) : List[Observation] = {
    if(dataset == null)
      throw new IllegalArgumentException("")
    var observations = ListBuffer[Observation]()
    val sheet = workbook.getSheet(dataset.id)
    if(sheet == null) 
      throw new IllegalArgumentException("There isn't data for dataset: " + 
          dataset.id)
    
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
    	    //TODO Create the observation with all data. Dataset, label, 
    	    //Area, Computation, Inidicator and status (Raw, Imputed, 
    	    //Normalised) in this order
        	observations += new Observation(null, "", null, null, null, 
        	    year.toInt, value.toDouble, "")
        }
      }
    }
    observations.toList
  } 
  
}