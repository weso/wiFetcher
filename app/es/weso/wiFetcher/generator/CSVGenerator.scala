package es.weso.wiFetcher.generator

import scala.collection.mutable.ListBuffer
import java.io.BufferedWriter
import java.io.FileWriter
import au.com.bytecode.opencsv.CSVWriter
import scala.collection.JavaConversions._

case class CSVGenerator (csvSchema : Array[String]){

  val path = "reports/report-"
    
  private val values : ListBuffer[Array[String]] = ListBuffer.empty 
  values += csvSchema
  
  def addValue(value : Array[String]) = {
    values += value
  }
  
  def save(timestamp : Long) : String = {
    val finalPath = path + timestamp.toString + ".csv" 
    val out = new BufferedWriter(new FileWriter("public/" + finalPath))
    val writer = new CSVWriter(out)
    try {
      writer.writeAll(values.toList)
    } finally {
      writer.close
    }
    finalPath
  }
  
}