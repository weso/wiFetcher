package es.weso.wiFetcher.dao

import scala.collection.immutable.List
import es.weso.wiFetcher.entities.Region
import es.weso.wiFetcher.utils.FileUtils
import scala.collection.mutable.ListBuffer
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.io.File
import org.apache.poi.hssf.util.CellReference
import es.weso.wiFetcher.configuration.Configuration
import scala.collection.mutable.HashSet
import es.weso.wiFetcher.utils.POIUtils
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

class RegionDAOImpl(path : String, relativePath : Boolean) extends RegionDAO {
  
  private val SHEET_NAME = "Geo"
    
  private var regions : List[Region] = load(FileUtils.getFilePath(path, 
      relativePath))
  
  private def load(path : String) : List[Region] = {
    val regions : HashSet[Region] = new HashSet[Region]
    val workbook = WorkbookFactory.create(new FileInputStream(new File(path)))
    val sheet : Sheet = workbook.getSheet(SHEET_NAME)
    if(sheet == null) 
      throw new IllegalArgumentException("Not exist a sheet in the file " + 
          path + " with the name " + SHEET_NAME)
    val cellReference = new CellReference(
        Configuration.getRegionInitialCell)
    for(row <- cellReference.getRow() to sheet.getLastRowNum()) {
      var regionName = POIUtils.extractCellValue(sheet.getRow(row).getCell(
          Configuration.getRegionNameColumn))
      var region = new Region
      region.name = regionName
      regions.add(region)
      var country = POIUtils.extractCellValue(sheet.getRow(row).getCell(
          Configuration.getRegionCountryColumn))
      region = regions.find(region => region.name.equals(regionName)).getOrElse(throw new IllegalArgumentException)
      region.addCountry(SpreadsheetsFetcher.obtainCountry(country))
    }
    regions.toList
  }
  
  def getRegions() : List[Region] = {
    regions
  }

}