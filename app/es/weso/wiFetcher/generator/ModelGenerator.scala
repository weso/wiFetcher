package es.weso.wiFetcher.generator
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.ResourceFactory
import es.weso.wiFetcher.persistence.jena._
import es.weso.wiFetcher.entities.traits.Component
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.traits.SubIndex
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.entities.Region
import scala.collection.mutable.ListBuffer
import es.weso.wiFetcher.utils.DateUtils
import java.util.Date
import java.io.FileOutputStream
import java.io.File
import es.weso.wiFetcher.utils.IssueManagerUtils

object ModelGenerator {

  val PREFIX_OBS = "http://data.webfoundation.org/webindex/v2013/observation/"
  val PREFIX_WI_ONTO = "http://data.webfoundation.org/webindex/ontology/"
  val PREFIX_DCTERMS = "http://purl.org/dc/terms/"
  val PREFIX_WI_ORG = "http://data.webfoundation.org/webindex/organization/"
  val PREFIX_QB = "http://purl.org/linked-data/cube#"
  val PREFIX_COUNTRY = "http://data.webfoundation.org/webindex/v2013/country/"
  val PREFIX_REGION = "http://data.webfoundation.org/webindex/v2013/region/"
  val PREFIX_CEX = "http://purl.org/weso/ontology/computex#"
  val PREFIX_INDICATOR = "http://data.webfoundation.org/webindex/v2013/indicator/"
  val PREFIX_SDMX_CONCEPT = "http://purl.org/linked-data/sdmx/2009/concept#"
  val PREFIX_SDMX_CODE = "http://purl.org/linked-data/sdmx/2009/code#"
  val PREFIX_DATASET = "http://data.webfoundation.org/webindex/v2013/dataset/"
  val PREFIX_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val PREFIX_RDFS = "http://www.w3.org/2000/01/rdf-schema#"
  val PREFIX_COMPONENT = "http://data.webfoundation.org/webindex/v2013/component/"
  val PREFIX_TIME = "http://www.w3.org/2006/time#"
  val PREFIX_SKOS = "http://www.w3.org/2004/02/skos/core#"
  val PREFIX_SUBINDEX = "http://data.webfoundation.org/webindex/v2013/subindex/"
  val PREFIX_WEIGHTSCHEMA = "http://data.webfoundation.org/webindex/v2013/weightSchema/"
  val PREFIX_SLICE = "http://data.webfoundation.org/webindex/v2013/slice/"
  val PREFIX_SMDX_ATTRIBUTE = "http://purl.org/linked-data/sdmx/2009/attribute#"
  val PREFIX_SMDX_SUBJECT = "http://purl.org/linked-data/sdmx/2009/subject#"

  val PROPERTY_RDF_TYPE = ResourceFactory.createProperty(PREFIX_RDF + "type")
  val PROPERTY_RDFS_LABEL = ResourceFactory.createProperty(PREFIX_RDFS
    + "label")
  val PROPERTY_DCTERMS_PUBLISHER = ResourceFactory.createProperty(PREFIX_DCTERMS
    + "publisher")
  val PROPERTY_DCTERMS_CONTRIBUTOR = ResourceFactory.createProperty(PREFIX_DCTERMS
    + "contributor")
  val PROPERTY_WIONTO_REFAREA = ResourceFactory.createProperty(PREFIX_WI_ONTO
    + "ref-area")
  val PROPERTY_WIONTO_REFCOMPUTATION = ResourceFactory.createProperty(PREFIX_WI_ONTO
    + "ref-computation")
  val PROPERTY_CEX_INDICATOR = ResourceFactory.createProperty(PREFIX_CEX
    + "indicator")
  val PROPERTY_WIONTO_REFYEAR = ResourceFactory.createProperty(PREFIX_WI_ONTO
    + "ref-year")
  val PROPERTY_CEX_VALUE = ResourceFactory.createProperty(PREFIX_CEX
    + "value")
  val PROPERTY_SMDX_OBSSTATUS = ResourceFactory.createProperty(PREFIX_SDMX_CONCEPT
    + "obsStatus")
  val PROPERTY_QB_DATASET = ResourceFactory.createProperty(PREFIX_QB
    + "dataSet")
  val PROPERTY_WIONTO_SHEETTYPE = ResourceFactory.createProperty(PREFIX_WI_ONTO
    + "sheet-type")
  val PROPERTY_CEX_MD5 = ResourceFactory.createProperty(PREFIX_CEX + "md5-checksum")
  val PROPERTY_CEX_COMPONENT = ResourceFactory.createProperty(PREFIX_CEX + "component")
  val PROPERTY_CEX_HIGHLOW = ResourceFactory.createProperty(PREFIX_CEX + "highLow")
  val PROPERTY_DCTERMS_SOURCE = ResourceFactory.createProperty(PREFIX_DCTERMS + "source")
  val PROPERTY_RDFS_COMMENT = ResourceFactory.createProperty(PREFIX_RDFS + "comment")
  val PROPERTY_TIME_STARTS = ResourceFactory.createProperty(PREFIX_TIME + "intervalStarts")
  val PROPERTY_TIME_FINISHES = ResourceFactory.createProperty(PREFIX_TIME + "intervalFinishes")
  val PROPERTY_WIONTO_COUNTRYCOVERAGE = ResourceFactory.createProperty(PREFIX_WI_ONTO + "country-coverage")
  val PROPERTY_WIONTO_PROVIDERLINK = ResourceFactory.createProperty(PREFIX_WI_ONTO + "provider-link")
  val PROPERTY_WIONTO_REFSOURCE = ResourceFactory.createProperty(PREFIX_WI_ONTO + "ref-source")
  val PROPERTY_SKOS_NOTATION = ResourceFactory.createProperty(PREFIX_SKOS + "notation")
  val PROPERTY_SKOS_DEFINITION = ResourceFactory.createProperty(PREFIX_SKOS + "definition")
  val PROPERTY_SKOS_HASCOMPONENT = ResourceFactory.createProperty(PREFIX_SKOS + "has-component")
  //val PROPERTY_DCTERMS_CREATED = ResourceFactory.createProperty(PREFIX_DCTERMS + "created")
  val PROPERTY_DCTERMS_ISSUED = ResourceFactory.createProperty(PREFIX_DCTERMS + "issued")
  val PROPERTY_CEX_ELEMENT = ResourceFactory.createProperty(PREFIX_CEX + "element")
  val PROPERTY_CEX_WEIGHT = ResourceFactory.createProperty(PREFIX_CEX + "weight")
  val PROPERTY_QB_DIMENSION = ResourceFactory.createProperty(PREFIX_QB + "dimension")
  val PROPERTY_QB_ORDER = ResourceFactory.createProperty(PREFIX_QB + "order")
  val PROPERTY_QB_MEASURE = ResourceFactory.createProperty(PREFIX_QB + "measure")
  val PROPERTY_QB_COMPONENT = ResourceFactory.createProperty(PREFIX_QB + "component")
  val PROPERTY_QB_SLICESTRUCTURE = ResourceFactory.createProperty(PREFIX_QB + "sliceStructure")
  val PROPERTY_QB_OBSERVATION = ResourceFactory.createProperty(PREFIX_QB + "observation")
  val PROPERTY_WIONTO_ISO2 = ResourceFactory.createProperty(PREFIX_WI_ONTO + "has-iso-alpha2-code")
  val PROPERTY_WIONTO_ISO3 = ResourceFactory.createProperty(PREFIX_WI_ONTO + "has-iso-alpha3-code")
  val PROPERTY_QB_ATTRIBUTE = ResourceFactory.createProperty(PREFIX_QB + "attribute")
  val PROPERTY_QB_COMPONENT_REQUIRED = ResourceFactory.createProperty(PREFIX_QB + "componentRequired")
  val PROPERTY_QB_COMPONENT_ATTACHMENT = ResourceFactory.createProperty(PREFIX_QB + "componentAttachment")
  val PROPERTY_QB_SLICEKEY = ResourceFactory.createProperty(PREFIX_QB + "sliceKey")
  val PROPERTY_QB_COMPONENTPROPERTY = ResourceFactory.createProperty(PREFIX_QB + "componentProperty")
  val PROPERTY_QB_STRUCTURE = ResourceFactory.createProperty(PREFIX_QB + "structure")
  val PROPERTY_DCTERMS_TITLE = ResourceFactory.createProperty(PREFIX_DCTERMS + "title")
  val PROPERTY_DCTERMS_SUBJECT = ResourceFactory.createProperty(PREFIX_DCTERMS + "subject")
  val PROPERTY_SMDX_UNITMEASURE = ResourceFactory.createProperty(PREFIX_SMDX_ATTRIBUTE + "unitMeasure")
  val PROPERTY_QB_SLICE = ResourceFactory.createProperty(PREFIX_QB + "slice")

  def generateJenaModel(spreadsheetsFetcher:SpreadsheetsFetcher): String = {
    //val observations : List[Observation] = SpreadsheetsFetcher.observations.toList
    val observationsByDataset = spreadsheetsFetcher.observations.groupBy(
      observation => observation.dataset)
    val model = createModel
    createDataStructureDefinition(model)
    spreadsheetsFetcher.primaryIndicators.toList.foreach(
      indicator => createPrimaryIndicatorTriples(indicator, model))
    spreadsheetsFetcher.secondaryIndicators.toList.foreach(
      indicator => createSecondaryIndicatorTriples(indicator, model))
    spreadsheetsFetcher.components.foreach(
      comp => createComponentsTriples(comp, model))
    spreadsheetsFetcher.subIndexes.foreach(
      subindex => createSubindexTriples(subindex, model))
    spreadsheetsFetcher.datasets.foreach(
      dataset => createDatasetsTriples(dataset, observationsByDataset, model))
    spreadsheetsFetcher.countries.foreach(
      country => createCountriesTriples(country, model))
    spreadsheetsFetcher.regions.foreach(
      region => createRegionsTriples(region, model))

    //storeModel(model)
    saveModel(model)

  }

  private def saveModel(model: Model): String = {
    val timestamp = new Date().getTime()
    val path = s"reports/dataset-${timestamp}.ttl"
    model.write(new FileOutputStream(new File(s"public/${path}")), "TURTLE")
    path
  }

  private def storeModel(model: Model): Model = {
    JenaModelDAOImpl.store(model)
    model
  }

  def createDataStructureDefinition(model: Model) = {
    val dsd = model.createResource(PREFIX_WI_ONTO + "DSD")
    dsd.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_QB + "DataStructureDefinition"))
    var dimension = model.createResource()
    dimension.addProperty(PROPERTY_QB_DIMENSION, ResourceFactory.createResource(PREFIX_WI_ONTO + "ref-area"))
    dimension.addProperty(PROPERTY_QB_ORDER, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger))
    dsd.addProperty(PROPERTY_QB_COMPONENT, dimension)
    dimension = model.createResource()
    dimension.addProperty(PROPERTY_QB_DIMENSION, ResourceFactory.createResource(PREFIX_WI_ONTO + "ref-year"))
    dimension.addProperty(PROPERTY_QB_ORDER, ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger))
    dsd.addProperty(PROPERTY_QB_COMPONENT, dimension)
    dimension = model.createResource()
    dimension.addProperty(PROPERTY_QB_DIMENSION, ResourceFactory.createResource(PREFIX_CEX + "indicator"))
    dimension.addProperty(PROPERTY_QB_ORDER, ResourceFactory.createTypedLiteral("3", XSDDatatype.XSDinteger))
    dsd.addProperty(PROPERTY_QB_COMPONENT, dimension)
    val measure = model.createResource()
    measure.addProperty(PROPERTY_QB_MEASURE, ResourceFactory.createResource(PREFIX_CEX + "value"))
    dsd.addProperty(PROPERTY_QB_COMPONENT, measure)
    val component = model.createResource()
    component.addProperty(PROPERTY_QB_ATTRIBUTE, ResourceFactory.createResource(PREFIX_SMDX_ATTRIBUTE + "unitMeasure"))
    component.addProperty(PROPERTY_QB_COMPONENT_REQUIRED, ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean))
    component.addProperty(PROPERTY_QB_COMPONENT_ATTACHMENT, ResourceFactory.createResource(PREFIX_QB + "DataSet"))
    dsd.addProperty(PROPERTY_QB_COMPONENT, component)
    val sliceByArea = model.createResource(PREFIX_WI_ONTO + "sliceByArea")
    dsd.addProperty(PROPERTY_QB_SLICEKEY, sliceByArea)
    sliceByArea.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_QB + "SliceKey"))
    sliceByArea.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral("slice by area", "en"))
    sliceByArea.addProperty(PROPERTY_RDFS_COMMENT, ResourceFactory.createLangLiteral("Slice by grouping areas together fixing year", "en"))
    sliceByArea.addProperty(PROPERTY_QB_COMPONENTPROPERTY, ResourceFactory.createResource(PREFIX_CEX + "indicator"))
    sliceByArea.addProperty(PROPERTY_QB_COMPONENTPROPERTY, ResourceFactory.createResource(PREFIX_WI_ONTO + "ref-year"))
  }

  def createRegionsTriples(region: Region, model: Model) = {
    val regionResource = model.createResource(PREFIX_REGION + region.name.replace("& ", "").replace(" ", "-"))
    regionResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_WI_ONTO + "Region"))
    regionResource.addProperty(PROPERTY_DCTERMS_CONTRIBUTOR, ResourceFactory.createResource(PREFIX_WI_ONTO + "WESO"))
    regionResource.addProperty(PROPERTY_DCTERMS_PUBLISHER, ResourceFactory.createResource(PREFIX_WI_ONTO + "WebFoundation"))
    regionResource.addProperty(PROPERTY_DCTERMS_ISSUED, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    regionResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createTypedLiteral(region.name, XSDDatatype.XSDstring))
    region.getCountries.foreach(country => regionResource.addProperty(PROPERTY_WIONTO_REFAREA, ResourceFactory.createResource(PREFIX_COUNTRY + country.iso3Code)))
  }

  def createCountriesTriples(country: Country, model: Model) = {
    val countryResource = model.createResource(PREFIX_COUNTRY + country.iso3Code)
    countryResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_WI_ONTO + "Country"))
    countryResource.addProperty(PROPERTY_CEX_MD5, ResourceFactory.createTypedLiteral("MD5 for " + country.iso3Code, XSDDatatype.XSDstring))
    countryResource.addProperty(PROPERTY_DCTERMS_CONTRIBUTOR, ResourceFactory.createResource(PREFIX_WI_ONTO + "WESO"))
    countryResource.addProperty(PROPERTY_DCTERMS_ISSUED, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    countryResource.addProperty(PROPERTY_DCTERMS_PUBLISHER, ResourceFactory.createResource(PREFIX_WI_ORG + "WebFoundation"))
    countryResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(country.name, "en"))
    countryResource.addProperty(PROPERTY_WIONTO_ISO2, ResourceFactory.createTypedLiteral(country.iso2Code, XSDDatatype.XSDstring))
    countryResource.addProperty(PROPERTY_WIONTO_ISO3, ResourceFactory.createTypedLiteral(country.iso3Code, XSDDatatype.XSDstring))
  }

  def createObservationTriples(obs: Observation, model: Model, id: Int) = {
    val obsResource = model.createResource(PREFIX_OBS + "obs" + /*obs.indicator.id + "-" + obs.area.iso2Code + "-" + obs.year.toString + "-" + obs.status*/ id)
    obsResource.addProperty(PROPERTY_RDF_TYPE,
      ResourceFactory.createResource(PREFIX_WI_ONTO + "Observation"))
    obsResource.addProperty(PROPERTY_DCTERMS_CONTRIBUTOR, ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    obsResource.addProperty(PROPERTY_DCTERMS_ISSUED, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    obsResource.addProperty(PROPERTY_DCTERMS_PUBLISHER, ResourceFactory.createResource(PREFIX_WI_ORG + "WebFoundation"))
    obsResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_QB + "Observation"))
    obsResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(obs.indicator.label + " in " + obs.area.iso3Code +
      " during " + obs.year, "en"))
    obsResource.addProperty(PROPERTY_WIONTO_REFAREA, ResourceFactory.createResource(PREFIX_COUNTRY + obs.area.iso3Code))
    obsResource.addProperty(PROPERTY_WIONTO_REFCOMPUTATION, ResourceFactory.createResource(PREFIX_CEX + obs.status))
    obsResource.addProperty(PROPERTY_CEX_INDICATOR, ResourceFactory.createResource(PREFIX_INDICATOR + obs.indicator.id))
    obsResource.addProperty(PROPERTY_WIONTO_REFYEAR, ResourceFactory.createTypedLiteral(
      String.valueOf(obs.year), XSDDatatype.XSDinteger))
    obsResource.addProperty(PROPERTY_CEX_VALUE, ResourceFactory.createTypedLiteral(
      String.valueOf(obs.value), XSDDatatype.XSDdouble))
    obsResource.addProperty(PROPERTY_SMDX_OBSSTATUS, ResourceFactory.createResource(PREFIX_CEX + obs.status))
    obsResource.addProperty(PROPERTY_QB_DATASET, ResourceFactory.createResource(PREFIX_DATASET + obs.dataset.id))
    obsResource.addProperty(PROPERTY_WIONTO_SHEETTYPE, ResourceFactory.createResource(PREFIX_WI_ONTO + obs.status))
    obsResource.addProperty(PROPERTY_CEX_MD5, ResourceFactory.createLangLiteral(
      "MD5 checksum for observation " + id, "en"))
  }

  def createSecondaryIndicatorTriples(indicator: Indicator, model: Model) = {
    val indicatorResource = model.createResource(PREFIX_INDICATOR + indicator.id.replace(" ", ""))
    indicatorResource.addProperty(PROPERTY_CEX_MD5,
      ResourceFactory.createLangLiteral("MD5 checksum for indicator " + indicator.id, "en"))
    indicatorResource.addProperty(PROPERTY_CEX_COMPONENT,
      ResourceFactory.createResource(PREFIX_COMPONENT + indicator.component.id))
    indicatorResource.addProperty(PROPERTY_CEX_HIGHLOW, ResourceFactory.createResource(PREFIX_CEX + indicator.highLow))
    indicatorResource.addProperty(PROPERTY_DCTERMS_SOURCE, indicator.source)
    indicatorResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "Indicator"))
    indicatorResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_WI_ONTO + indicator.indicatorType + "Indicator"))
    indicatorResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(indicator.label, "en"))
    indicatorResource.addProperty(PROPERTY_RDFS_COMMENT, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PROPERTY_TIME_STARTS, ResourceFactory.createTypedLiteral("2009", XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PROPERTY_TIME_FINISHES, ResourceFactory.createTypedLiteral("2012", XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PROPERTY_WIONTO_COUNTRYCOVERAGE, ResourceFactory.createTypedLiteral(indicator.countriesCoverage.toString, XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PROPERTY_WIONTO_PROVIDERLINK, ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    indicatorResource.addProperty(PROPERTY_WIONTO_REFSOURCE, ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    createIndicatorWeightTriples(indicator, model)
  }

  def createPrimaryIndicatorTriples(indicator: Indicator, model: Model) = {
    val indicatorResource = model.createResource(PREFIX_INDICATOR + indicator.id.replace(" ", ""))
    indicatorResource.addProperty(PROPERTY_CEX_MD5,
      ResourceFactory.createLangLiteral("MD5 checksum for indicator " + indicator.id, "en"))
    indicatorResource.addProperty(PROPERTY_CEX_COMPONENT,
      ResourceFactory.createResource(PREFIX_COMPONENT + indicator.component.id))
    indicatorResource.addProperty(PROPERTY_CEX_HIGHLOW, ResourceFactory.createResource(PREFIX_CEX + indicator.highLow))
    indicatorResource.addProperty(PROPERTY_DCTERMS_SOURCE, indicator.source)
    indicatorResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "Indicator"))
    indicatorResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_WI_ONTO + indicator.indicatorType + "Indicator"))
    indicatorResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(indicator.label, "en"))
    indicatorResource.addProperty(PROPERTY_RDFS_COMMENT, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PROPERTY_SKOS_NOTATION, ResourceFactory.createTypedLiteral(indicator.id, XSDDatatype.XSDstring))
    indicatorResource.addProperty(PROPERTY_SKOS_DEFINITION, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PROPERTY_TIME_STARTS, ResourceFactory.createTypedLiteral("2011", XSDDatatype.XSDint))
    indicatorResource.addProperty(PROPERTY_TIME_FINISHES, ResourceFactory.createTypedLiteral("2011", XSDDatatype.XSDint))
    indicatorResource.addProperty(PROPERTY_WIONTO_REFSOURCE, ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    createIndicatorWeightTriples(indicator, model)
  }

  def createIndicatorWeightTriples(indicator: Indicator, model: Model) = {
    val weightResource = model.getResource(PREFIX_WEIGHTSCHEMA + "indicatorWeights")
    weightResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "WeightSchema"))
    val anonymousResource = model.createResource()
    anonymousResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "Weight"))
    anonymousResource.addProperty(PROPERTY_CEX_ELEMENT, ResourceFactory.createResource(PREFIX_INDICATOR + indicator.id))
    anonymousResource.addProperty(PROPERTY_CEX_VALUE, ResourceFactory.createTypedLiteral(indicator.weight.toString, XSDDatatype.XSDdouble))
    weightResource.addProperty(PROPERTY_CEX_WEIGHT, anonymousResource)
  }

  def createComponentsTriples(component: Component, model: Model) = {
    val componentResource = model.createResource(PREFIX_COMPONENT + component.id)
    componentResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "Component"))
    componentResource.addProperty(PROPERTY_CEX_MD5, ResourceFactory.createLangLiteral("MD5 for" + component.name, "en"))
    componentResource.addProperty(PROPERTY_DCTERMS_CONTRIBUTOR, ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    componentResource.addProperty(PROPERTY_DCTERMS_ISSUED, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    componentResource.addProperty(PROPERTY_DCTERMS_PUBLISHER, ResourceFactory.createResource(PREFIX_WI_ORG + "WebFoundation"))
    componentResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(component.name, "en"))
    componentResource.addProperty(PROPERTY_DCTERMS_ISSUED, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    component.getIndicators.foreach(indicator => {
      componentResource.addProperty(PROPERTY_CEX_ELEMENT, ResourceFactory.createResource(PREFIX_INDICATOR + indicator.id))
    })

    val weightComponent = model.createResource(PREFIX_WEIGHTSCHEMA + "componentWeights")
    weightComponent.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "WeightSchema"))
    val anonymousResource = model.createResource()
    anonymousResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "Weight"))
    anonymousResource.addProperty(PROPERTY_CEX_ELEMENT, ResourceFactory.createResource(PREFIX_COMPONENT + component.id))
    anonymousResource.addProperty(PROPERTY_CEX_VALUE, ResourceFactory.createTypedLiteral(component.weight.toString, XSDDatatype.XSDdouble))
    weightComponent.addProperty(PROPERTY_CEX_WEIGHT, anonymousResource)
  }

  def createSubindexTriples(subindex: SubIndex, model: Model) {
    val subindexResource = model.createResource(PREFIX_SUBINDEX + subindex.id)
    subindexResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "SubIndex"))
    subindexResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(subindex.name, "en"))
    subindexResource.addProperty(PROPERTY_RDFS_COMMENT, ResourceFactory.createLangLiteral(subindex.description, "en"))
    subindex.getComponents.foreach(component => {
      subindexResource.addProperty(PROPERTY_CEX_ELEMENT, ResourceFactory.createResource(PREFIX_COMPONENT + component.id))
    })

    val weightSubindex = model.createResource(PREFIX_WEIGHTSCHEMA + "subindexWeights")
    weightSubindex.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX))
    val anonymousResource = model.createResource
    anonymousResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_CEX + "Weight"))
    anonymousResource.addProperty(PROPERTY_CEX_ELEMENT, ResourceFactory.createResource(PREFIX_SUBINDEX + subindex.id))
    anonymousResource.addProperty(PROPERTY_CEX_VALUE, ResourceFactory.createTypedLiteral(subindex.weight.toString, XSDDatatype.XSDdouble))
    weightSubindex.addProperty(PROPERTY_CEX_WEIGHT, anonymousResource)
  }

  private def createDatasetsTriples(dataset: Dataset,
    observationsByDataset: Map[Dataset, ListBuffer[Observation]],
    model: Model) = {
    val datasetResource = model.createResource(PREFIX_DATASET + dataset.id)
    datasetResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_QB + "DataSet"))
    datasetResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_WI_ONTO + "Dataset"))
    datasetResource.addProperty(PROPERTY_CEX_MD5, ResourceFactory.createTypedLiteral("MD5...", XSDDatatype.XSDstring))
    datasetResource.addProperty(PROPERTY_DCTERMS_CONTRIBUTOR, ResourceFactory.createResource(PREFIX_WI_ORG + "WESO"))
    datasetResource.addProperty(PROPERTY_DCTERMS_ISSUED, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    datasetResource.addProperty(PROPERTY_DCTERMS_PUBLISHER, ResourceFactory.createResource(PREFIX_WI_ORG + "WebFoundation"))
    datasetResource.addProperty(PROPERTY_DCTERMS_TITLE, ResourceFactory.createLangLiteral(dataset.id, "en"))
    datasetResource.addProperty(PROPERTY_DCTERMS_SUBJECT, ResourceFactory.createResource(PREFIX_SMDX_SUBJECT + "2.5"))
    datasetResource.addProperty(PROPERTY_RDFS_LABEL, ResourceFactory.createLangLiteral(dataset.id, "en"))
    datasetResource.addProperty(PROPERTY_RDFS_COMMENT, ResourceFactory.createLangLiteral("Description of dataset " + dataset.id, "en"))
    datasetResource.addProperty(PROPERTY_SMDX_UNITMEASURE, ResourceFactory.createResource("http://dbpedia.org/resource/Year"))
    var anonymousResource = model.createResource()
    anonymousResource.addProperty(PROPERTY_QB_DIMENSION, ResourceFactory.createResource(PREFIX_WI_ONTO + "ref-area"))
    anonymousResource.addProperty(PROPERTY_QB_ORDER, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PROPERTY_QB_COMPONENT, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PROPERTY_QB_DIMENSION, ResourceFactory.createResource(PREFIX_WI_ONTO + "ref-area"))
    anonymousResource.addProperty(PROPERTY_QB_ORDER, ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PROPERTY_QB_COMPONENT, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PROPERTY_QB_DIMENSION, ResourceFactory.createResource(PREFIX_WI_ONTO + "ref-area"))
    anonymousResource.addProperty(PROPERTY_QB_ORDER, ResourceFactory.createTypedLiteral("3", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PROPERTY_QB_COMPONENT, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PROPERTY_QB_MEASURE, ResourceFactory.createResource(PREFIX_CEX + "indicator"))
    datasetResource.addProperty(PROPERTY_QB_COMPONENT, anonymousResource)
    datasetResource.addProperty(PROPERTY_QB_STRUCTURE, ResourceFactory.createResource(PREFIX_WI_ONTO + "DSD"))

    observationsByDataset.get(dataset) match {
      case Some(observations) =>
        val observationsByYear: Map[Int, ListBuffer[Observation]] = observations.groupBy(observation => observation.year)
        observationsByYear.keySet.foreach(year => {
          val sliceResource = model.createResource(PREFIX_SLICE + "Slice-" +
            observations.head.indicator.id + year.toString + "-" + observations.head.status)
          sliceResource.addProperty(PROPERTY_RDF_TYPE, ResourceFactory.createResource(PREFIX_QB + "Slice"))
          sliceResource.addProperty(PROPERTY_CEX_INDICATOR, ResourceFactory.createResource(PREFIX_INDICATOR +
            observations.head.indicator.id))
          sliceResource.addProperty(PROPERTY_WIONTO_REFYEAR, ResourceFactory.createTypedLiteral(year.toString, XSDDatatype.XSDinteger))
          sliceResource.addProperty(PROPERTY_QB_SLICESTRUCTURE, /*datasetResource*/ ResourceFactory.createResource(PREFIX_WI_ONTO +
            "sliceByArea"))
          observationsByYear.get(year).getOrElse(throw new IllegalArgumentException).zipWithIndex.foreach(obs => {
            val id = obs._2+1
            createObservationTriples(obs._1, model, id)
            sliceResource.addProperty(PROPERTY_QB_OBSERVATION, ResourceFactory.createResource(PREFIX_OBS + "obs" +  id))
          })
          datasetResource.addProperty(PROPERTY_QB_SLICE, sliceResource)
        })
      case None => IssueManagerUtils.addError(message=s"No observations for the dataset ${dataset.id}", path = Some("RAW File"))
    }
  }

  private def createModel: com.hp.hpl.jena.rdf.model.Model = {
    val model = ModelFactory.createDefaultModel
    model.setNsPrefix("obs", PREFIX_OBS)
    model.setNsPrefix("wi-onto", PREFIX_WI_ONTO)
    model.setNsPrefix("dcterms", PREFIX_DCTERMS)
    model.setNsPrefix("wi-org", PREFIX_WI_ORG)
    model.setNsPrefix("qb", PREFIX_QB)
    model.setNsPrefix("country", PREFIX_COUNTRY)
    model.setNsPrefix("cex", PREFIX_CEX)
    model.setNsPrefix("indicator", PREFIX_INDICATOR)
    model.setNsPrefix("sdmx-concept", PREFIX_SDMX_CONCEPT)
    model.setNsPrefix("sdmx-code", PREFIX_SDMX_CODE)
    model.setNsPrefix("dataset", PREFIX_DATASET)
    model.setNsPrefix("rdf", PREFIX_RDF)
    model.setNsPrefix("rdfs", PREFIX_RDFS)
    model.setNsPrefix("component", PREFIX_COMPONENT)
    model.setNsPrefix("subindex", PREFIX_SUBINDEX)
    model.setNsPrefix("weightSchema", PREFIX_WEIGHTSCHEMA)
    model.setNsPrefix("slice", PREFIX_SLICE)
    model.setNsPrefix("region", PREFIX_REGION)
    model.setNsPrefix("skos", PREFIX_SKOS)
    model.setNsPrefix("time", PREFIX_TIME)
    model.setNsPrefix("smdx-attribute", PREFIX_SMDX_ATTRIBUTE)
    model.setNsPrefix("smdx-subject", PREFIX_SMDX_SUBJECT)
    model
  }

}
