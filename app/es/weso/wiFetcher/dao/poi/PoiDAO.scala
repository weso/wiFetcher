package es.weso.wiFetcher.dao.poi

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import es.weso.wiFetcher.dao.DAO
import java.io.InputStream

trait PoiDAO[T] extends DAO[T]{
  
  protected def load(is: InputStream): Unit
  
  protected def parseData(workbook: Workbook, sheet: Sheet): Seq[T]
  
}