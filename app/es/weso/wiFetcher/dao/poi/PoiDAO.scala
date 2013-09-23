package es.weso.wiFetcher.dao.poi

import java.io.InputStream

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import es.weso.wiFetcher.dao.DAO

trait PoiDAO[T] extends DAO[T]{
  
  protected def load(is: InputStream): Unit
  
  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[T]
  
}