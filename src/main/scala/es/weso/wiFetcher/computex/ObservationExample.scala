package es.weso.wiFetcher.computex

import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.weso.wiFetcher.entities.IndicatorType
import com.hp.hpl.jena.vocabulary.RDF
import com.hp.hpl.jena.rdf.model.ResourceFactory
import java.util.GregorianCalendar
import java.util.Date
import com.hp.hpl.jena.vocabulary.RDFS
import com.hp.hpl.jena.datatypes.RDFDatatype
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype

object ObservationExample {
  
  val PREFIX_OBS = "http://data.webfoundation.org/webindex/v2013/observation/"
  val PREFIX_WI_ONTO = "http://data.webfoundation.org/webindex/ontology/"
  val PREFIX_DCTERMS = "http://purl.org/dc/terms/"
  val PREFIX_WI_ORG = "http://data.webfoundation.org/webindex/organization/"
  val PREFIX_QB = "http://purl.org/linked-data/cube#"
  val PREFIX_COUNTRY = "http://data.webfoundation.org/webindex/v2013/country/"
  val PREFIX_CEX = "http://purl.org/weso/ontology/computex#"
  val PREFIX_INDICATOR = "http://data.webfoundation.org/webindex/v2013/indicator/"  

  def main(args: Array[String]): Unit = {
    /*val fetcher : SpreadsheetsFetcher = new SpreadsheetsFetcher
    fetcher.loadWorkbook("computex/example.xlsx", true)
    var datasets : List[Dataset] = createDatasets()
    var observations : List[Observation] = fetcher.getObservations(datasets)
    var model : Model = ModelFactory.createDefaultModel
    model.setNsPrefix("obs", PREFIX_OBS)
    model.setNsPrefix("wi-onto", PREFIX_WI_ONTO)
    model.setNsPrefix("dcterms", PREFIX_DCTERMS)
    model.setNsPrefix("wi-org", PREFIX_WI_ORG)
    model.setNsPrefix("qb", PREFIX_QB)
    model.setNsPrefix("country", PREFIX_COUNTRY)
    model.setNsPrefix("cex", PREFIX_CEX)
    model.setNsPrefix("indicator", PREFIX_INDICATOR)
    println("Numero de observaciones: " + observations.size)
    println(model)
    observations.foreach(obs => createTriples(obs, model))*/
  }
  
  def createDatasets() : List[Dataset] = {
    val dataset1 = new Dataset
    dataset1.id = "A-RAW"
    val dataset2 = new Dataset
    dataset2.id = "B-RAW"
    val dataset3 = new Dataset
    dataset3.id = "C-RAW"
    val dataset4 = new Dataset
    dataset4.id = "D-RAW"
    val dataset5 = new Dataset
    dataset5.id = "Q1"
    val dataset6 = new Dataset
    dataset6.id = "Q2"
    val dataset7 = new Dataset
    dataset7.id = "A-Imputed"
    val dataset8 = new Dataset
    dataset8.id = "C-IMPUTED"
    val dataset9 = new Dataset
    dataset9.id = "A-NORMALISED"
    List[Dataset](dataset1, dataset2, dataset3, dataset4, dataset5, dataset6, 
        dataset7, dataset8, dataset9)
  }
  
  def createTriples(obs : Observation, model : Model) = {
    println("create triple")
    var name : String  = obtainName(obs)
    var obsResource = model.createResource(PREFIX_OBS + name)
    obsResource.addProperty(ResourceFactory.createProperty(RDF.getURI() + "type"), 
        ResourceFactory.createResource(PREFIX_WI_ONTO + "Observation"))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_DCTERMS 
        + "contrubutor"), ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_DCTERMS 
        + "created"), "")
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_DCTERMS 
        + "publisher"), ResourceFactory.createResource(PREFIX_WI_ORG + "WebFoundation"))
    obsResource.addProperty(ResourceFactory.createProperty(RDF.getURI() 
        + "type"), ResourceFactory.createResource(PREFIX_QB + "Observation"))
    obsResource.addProperty(ResourceFactory.createProperty(RDFS.getURI() 
        + "label"), ResourceFactory.createLangLiteral(obs.indicator.label + " in " + obs.area.iso3Code + 
        " during " + obs.year, "en"))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_WI_ONTO 
        + "ref-area"), ResourceFactory.createResource(PREFIX_COUNTRY + obs.area.iso3Code))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_WI_ONTO 
        + "ref-computation"), ResourceFactory.createResource(PREFIX_CEX + obs.status))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_WI_ONTO 
        + "ref-indicator"), ResourceFactory.createResource(PREFIX_INDICATOR + obs.indicator.label))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_WI_ONTO 
        + "ref-year"), ResourceFactory.createTypedLiteral(
            String.valueOf(obs.year), XSDDatatype.XSDinteger))
    obsResource.addProperty(ResourceFactory.createProperty(PREFIX_WI_ONTO 
        + "value"), ResourceFactory.createTypedLiteral(
            String.valueOf(obs.value), XSDDatatype.XSDdouble))
    model.write(System.out, "N3-TRIPLE")
  }
  
  def obtainName(obs : Observation) : String = {
    var first : String = obs.area.iso3Code.charAt(0) + ""
    if(obs.indicator.indicatorType.equals(IndicatorType.Primary)) {
      first + obs.indicator.label
    } else {
      var second : String = obs.indicator.label
      var year = obs.year.toString
      first + second + year.substring(year.length() - 2)
    }
  }

}