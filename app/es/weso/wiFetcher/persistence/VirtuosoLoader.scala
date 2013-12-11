package es.weso.wiFetcher.persistence

import es.weso.wiFetcher.configuration.Configuration
import scala.sys.process._
import java.io.File
import java.io.PrintWriter
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer

object VirtuosoLoader {
  
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
  private val omittedWarnings : Array[String] = Array(
      "public/temp/script.sh: 1: public/temp/script.sh: [[: not found",
      "fatal: destination path 'wiExtract' already exists and is not an empty directory.",
      "Ya está en «master»",
      "log4j:WARN No appenders could be found for logger (org.apache.jena.riot.stream.JenaIOEnvironment).",
      "log4j:WARN Please initialize the log4j system properly.",
      "log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.",
      "Already on 'master'")

  def store() : List[String] = {  
    val errors : ListBuffer[String] = ListBuffer.empty
    val upload = Process("public/temp/script.sh")
    val processLogger = ProcessLogger((o: String) => logger.info(o),
        (e:String) => {
          if(!omittedWarnings.contains(e))
        	  errors += e
        })
    upload ! processLogger
    errors.toList
  }
  
  def generateCode(timestamp : Long, path : String, baseUri : String, namespace : String) = {
    val variables : StringBuilder = new StringBuilder
    val graph : String = baseUri + "/"
    val dir = Configuration.getVirtuosoLoadDir
    val virtServer = Configuration.getVirtuosoServer
    val virtUser = Configuration.getVirtuosoUser
    val virtPass = Configuration.getVirtuosoPass
    val scriptBuilder = new StringBuilder
    
    val file = "-f public/" + path
    val out = "-o public/temp/observations.json" 
      
    scriptBuilder.append("java -jar ./public/temp/WiExtract-assembly-1.0-SNAPSHOT.jar ")
    	.append(file).append(" ").append(out).append("\n")
    	
    val wiComputeInput = "-f public/" + path
    val wiComputeOut = "-o public/temp/" + namespace + "-computations.ttl"
    val wiComputeNamespace = "-n " + namespace
    val wiComputeYear = "-y 2013"
    scriptBuilder.append("java -jar ./public/temp/WiCompute-assembly-1.0-SNAPSHOT.jar ")
    	.append(wiComputeInput).append(" ").append(wiComputeOut).append(" ")
    	.append(wiComputeYear).append(" ").append(wiComputeNamespace).append("\n")
    
    scriptBuilder.append("wget -q https://raw.github.com/weso/computex/master/ontology/wf.ttl -O ./public/temp/wf.ttl\n")
    scriptBuilder.append("install ./public/temp/wf.ttl ").append(dir).append("\n")
    scriptBuilder.append("install ./public/temp/").append(namespace).append("-computations.ttl ").append(dir).append("\n")
    scriptBuilder.append("isql-vt ").append(virtServer).append(" ").append(virtUser).append(" ").append(virtPass).append(" <<EOF\n")
    scriptBuilder.append("sparql clear graph '").append(graph).append("';\n")
    scriptBuilder.append("delete from DB.DBA.load_list;\n")
    scriptBuilder.append("ld_dir ('").append(dir).append("', '").append(namespace).append("-computations.ttl', '").append(graph).append("');\n")
    scriptBuilder.append("ld_dir ('").append(dir).append("', 'wf.ttl', '").append(graph).append("');\n")
    scriptBuilder.append("rdf_loader_run();\n")
    scriptBuilder.append("EXIT;\n")
    scriptBuilder.append("EOF\n")
    scriptBuilder.append("echo \"flush_all\" | /bin/netcat -q 2 127.0.0.1 11211\n")
    
    val f = new File("public/temp/script.sh")
    val printWriter = new PrintWriter(f, "UTF-8")
    try {
      printWriter.print(scriptBuilder.toString)
    } finally {
      printWriter.close
    }
    
    val permission = Process("chmod u+x public/temp/script.sh")
    permission.run
  }
  
}