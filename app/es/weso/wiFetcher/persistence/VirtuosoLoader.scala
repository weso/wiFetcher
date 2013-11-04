package es.weso.wiFetcher.persistence

import es.weso.wiFetcher.configuration.Configuration
import scala.sys.process._
import java.io.File
import java.io.PrintWriter
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer
import es.weso.wiExtract._

object VirtuosoLoader {
  
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def store() : List[String] = {  
    val errors : ListBuffer[String] = ListBuffer.empty
    val upload = Process("public/temp/script.sh")
    val processLogger = ProcessLogger((o: String) => logger.info(o),
        (e:String) => errors += e)
    upload ! processLogger
    errors.toList
  }
  
  def generateCode(timestamp : Long, path : String, baseUri : String) = {
    val variables : StringBuilder = new StringBuilder
    val graph : String = baseUri + "/"
    val dir = Configuration.getVirtuosoLoadDir
    val virtServer = Configuration.getVirtuosoServer
    val virtUser = Configuration.getVirtuosoUser
    val virtPass = Configuration.getVirtuosoPass
    val scriptBuilder = new StringBuilder
    
    val file = "-f ./public/" + path
    val out = "-o ./public/temp/observations.json" 
      
    scriptBuilder.append("if [[ -d \"wiExtract\" && ! -L \"wiExtract\" ]] ; then \n")
    scriptBuilder.append("(cd wiExtract; git checkout \"master\")\n")
    scriptBuilder.append("(cd wiExtract; git pull origin \"master\")\n")
    scriptBuilder.append("else\n")
    scriptBuilder.append("git clone https://github.com/weso/wiExtract.git\n")
    scriptBuilder.append("(cd wiExtract ; git checkout master)\n")
    scriptBuilder.append("fi\n\n")
    
    scriptBuilder.append("(cd wiExtract ; sbt assembly)\n")
    scriptBuilder.append("java -jar ./wiExtract/target/scala-2.10/WiExtract-assembly-1.0-SNAPSHOT.jar ")
    	.append(file).append(" ").append(out)
    
    /*Main.main(Array(file, out))*/
    
    scriptBuilder.append("wget -q https://raw.github.com/weso/computex/master/ontology/wf.ttl -O ./public/temp/wf.ttl\n")
//    scriptBuilder.append("wget -q https://oss.sonatype.org/content/repositories/snapshots/es/weso/wiextract_2.10/1.0-SNAPSHOT/wiextract_2.10-1.0-SNAPSHOT.jar -O ./public/temp/wiExtract.jar \n")
//    scriptBuilder.append("java -jar ./public/temp/wiExtract.jar --file=./public/").append(path).append(" --out=./public/temp/").append(timestamp).append(".json \n")
    scriptBuilder.append("install ./public/temp/wf.ttl ").append(dir).append("\n")
    scriptBuilder.append("install ./public/").append(path).append(" ").append(dir).append("\n")
    scriptBuilder.append("isql-vt ").append(virtServer).append(" ").append(virtUser).append(" ").append(virtPass).append(" <<EOF\n")
    scriptBuilder.append("sparql clear graph '").append(graph).append("';\n")
    scriptBuilder.append("delete from DB.DBA.load_list;\n")
    scriptBuilder.append("ld_dir ('").append(dir).append("', 'dataset-").append(timestamp).append(".ttl', '").append(graph).append("');\n")
    scriptBuilder.append("ld_dir ('").append(dir).append("', 'wf.ttl', '").append(graph).append("');\n")
    scriptBuilder.append("rdf_loader_run();\n")
    scriptBuilder.append("EXIT;\n")
    scriptBuilder.append("EOF\n")
    
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