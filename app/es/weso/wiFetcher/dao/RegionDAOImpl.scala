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
import org.apache.log4j.Logger

/**
 * This class contains the implementation that allows to load all information
 * about the regions that are used by the Web Index.
 * 
 * At the moment, this class extracts the information of the regions from an
 * excel file that follows the structure of 2012 Web Index. Maybe we have to 
 * change the implementation  
 */
class RegionDAOImpl(path : String, relativePath : Boolean) extends RegionDAO {
  
  //The name of the sheet that contains the information about regions
  private val SHEET_NAME = "Geo"
  //A list with all regions  
  private var regions : List[Region] = load(FileUtils.getFilePath(path, 
      relativePath))
      
  private val logger : Logger = Logger.getLogger(this.getClass())
  
  /**
   * This method has to extract the information from an excel file. This file
   * contains the name of the regions and the countries that are part of them.
   * @param path the path of the file that contains the information
   * @return A list with all regions loaded
   */
  private def load(path : String) : List[Region] = {
    val regions : HashSet[Region] = new HashSet[Region]
    //Load the excel file
    val workbook = WorkbookFactory.create(new FileInputStream(new File(path)))
    //Obtain corresponding sheet
    val sheet : Sheet = workbook.getSheet(SHEET_NAME)
    if(sheet == null) {
      logger.error("Not exist a sheet in the file " + 
          path + " with the name " + SHEET_NAME)
      throw new IllegalArgumentException("Not exist a sheet in the file " + 
          path + " with the name " + SHEET_NAME)
    }
    //Obtain the first cell that contains data. This cell is in properties file
    val cellReference = new CellReference(
        Configuration.getRegionInitialCell)
    for(row <- cellReference.getRow() to sheet.getLastRowNum()) {
      logger.info("Begin extraction of region information")
      //Extract the name of the region. The column that contains the information
      //is in the properties file
      var regionName = POIUtils.extractCellValue(sheet.getRow(row).getCell(
          Configuration.getRegionNameColumn))
      var region = new Region
      region.name = regionName
      //Add the region in a set. In this manner, we avoid duplicates
      regions.add(region)
      //Extract the country name. The column that contains the name of the 
      //country in in the properties file
      var country = POIUtils.extractCellValue(sheet.getRow(row).getCell(
          Configuration.getRegionCountryColumn))
      //Obtain the unique region with the name extracted that is in the set, in 
      //order to add the countries at the same region
      region = regions.find(region => region.name.equals(regionName)).getOrElse(throw new IllegalArgumentException)
      //I add the country in the region information. First, we have to obtain
      //the country from SpreadsheetsFetcher given a name
      region.addCountry(SpreadsheetsFetcher.obtainCountry(country))
    }
    logger.info("Finish region extraction")
    regions.toList
  }
  
  /**
   * Returns a list of regions
   */
  def getRegions() : List[Region] = {
    regions
  }

}