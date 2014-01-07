package es.weso.wiFetcher.dao.poi

import java.io.InputStream

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import es.weso.wiFetcher.dao.DAO

/**
 * This is a generic trait for all loaders that load information from an excel
 * file
 */
trait PoiDAO[T] extends DAO[T]{
  
  protected def load(is: InputStream): Unit
  
  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[T]
  
}