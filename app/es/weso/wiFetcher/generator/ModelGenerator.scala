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

case class ModelGenerator(baseUri: String) {
  import ModelGenerator._

  val PrefixObs = new StringBuilder(baseUri)
    .append("webindex/v2013/observation/").toString
  val PrefixWiOnto = new StringBuilder(baseUri)
    .append("webindex/ontology/").toString
  val PrefixWiOrg = new StringBuilder(baseUri)
    .append("webindex/organization/").toString
  val PrefixCountry = new StringBuilder(baseUri)
    .append("webindex/v2013/country/").toString
  val PrefixRegion = new StringBuilder(baseUri)
    .append("webindex/v2013/region/").toString
  val PrefixIndicator = new StringBuilder(baseUri)
    .append("webindex/v2013/indicator/").toString
  val PrefixDataset = new StringBuilder(baseUri)
    .append("webindex/v2013/dataset/").toString
  val PrefixComponent = new StringBuilder(baseUri)
    .append("webindex/v2013/component/").toString
  val PrefixSubindex = new StringBuilder(baseUri)
    .append("webindex/v2013/subindex/").toString
  val PrefixWeightSchema = new StringBuilder(baseUri)
    .append("webindex/v2013/weightSchema/").toString
  val PrefixSlice = new StringBuilder(baseUri)
    .append("webindex/v2013/slice/").toString

  val PropertyWiOntoRefarea = ResourceFactory.createProperty(PrefixWiOnto
    + "ref-area")
  val PropertyWiOntoRefcomputation = ResourceFactory.createProperty(PrefixWiOnto
    + "ref-computation")
  val PropertyWiOntoRefYear = ResourceFactory.createProperty(PrefixWiOnto
    + "ref-year")
  val PropertyWiOntoShetType = ResourceFactory.createProperty(PrefixWiOnto
    + "sheet-type")
  val PropertyWiOntoCountryCoverage = ResourceFactory.createProperty(PrefixWiOnto + "country-coverage")
  val PropertyWiOntoProviderLink = ResourceFactory.createProperty(PrefixWiOnto + "provider-link")
  val PropertyWiOntoRefSource = ResourceFactory.createProperty(PrefixWiOnto + "ref-source")
  val PropertyWiOntoISO2 = ResourceFactory.createProperty(PrefixWiOnto + "has-iso-alpha2-code")
  val PropertyWiOntoISO3 = ResourceFactory.createProperty(PrefixWiOnto + "has-iso-alpha3-code")

  private var id: Int = 1

  def generateJenaModel(spreadsheetsFetcher: SpreadsheetsFetcher, store: Boolean, imp: Option[String] = None): String = {
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

    if (store) storeModel(model)

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
    val dsd = model.createResource(PrefixWiOnto + "DSD")
    dsd.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "DataStructureDefinition"))
    var dimension = model.createResource()
    dimension.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWiOnto + "ref-area"))
    dimension.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger))
    dsd.addProperty(PropertyQbComponent, dimension)
    dimension = model.createResource()
    dimension.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWiOnto + "ref-year"))
    dimension.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger))
    dsd.addProperty(PropertyQbComponent, dimension)
    dimension = model.createResource()
    dimension.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixCex + "indicator"))
    dimension.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("3", XSDDatatype.XSDinteger))
    dsd.addProperty(PropertyQbComponent, dimension)
    val measure = model.createResource()
    measure.addProperty(PropertyQbMeasure, ResourceFactory.createResource(PrefixCex + "value"))
    dsd.addProperty(PropertyQbComponent, measure)
    val component = model.createResource()
    component.addProperty(PropertyQbAttribute, ResourceFactory.createResource(PrefixSdmxAttribute + "unitMeasure"))
    component.addProperty(PropertyQbComponentRequired, ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean))
    component.addProperty(PropertyQbComponentAttachment, ResourceFactory.createResource(PrefixQb + "DataSet"))
    dsd.addProperty(PropertyQbComponent, component)
    val sliceByArea = model.createResource(PrefixWiOnto + "sliceByArea")
    dsd.addProperty(PropertyQbSliceKey, sliceByArea)
    sliceByArea.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "SliceKey"))
    sliceByArea.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral("slice by area", "en"))
    sliceByArea.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral("Slice by grouping areas together fixing year", "en"))
    sliceByArea.addProperty(PropertyQbComponentProperty, ResourceFactory.createResource(PrefixCex + "indicator"))
    sliceByArea.addProperty(PropertyQbComponentProperty, ResourceFactory.createResource(PrefixWiOnto + "ref-year"))
  }

  def createRegionsTriples(region: Region, model: Model) = {
    val regionResource = model.createResource(PrefixRegion + region.name.replace("& ", "").replace(" ", "_"))
    regionResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWiOnto + "Region"))
    regionResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWiOnto + "WESO"))
    regionResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWiOnto + "WebFoundation"))
    regionResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    regionResource.addProperty(PropertyRdfsLabel, ResourceFactory.createTypedLiteral(region.name, XSDDatatype.XSDstring))
    region.getCountries.foreach(country => regionResource.addProperty(PropertyWiOntoRefarea, ResourceFactory.createResource(PrefixCountry + country.iso3Code)))
  }

  def createCountriesTriples(country: Country, model: Model) = {
    val countryResource = model.createResource(PrefixCountry + country.iso3Code)
    countryResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWiOnto + "Country"))
    countryResource.addProperty(PropertyCexMD5, ResourceFactory.createTypedLiteral("MD5 for " + country.iso3Code, XSDDatatype.XSDstring))
    countryResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWiOnto + "WESO"))
    countryResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    countryResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWiOrg + "WebFoundation"))
    countryResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(country.name, "en"))
    countryResource.addProperty(PropertyWiOntoISO2, ResourceFactory.createTypedLiteral(country.iso2Code, XSDDatatype.XSDstring))
    countryResource.addProperty(PropertyWiOntoISO3, ResourceFactory.createTypedLiteral(country.iso3Code, XSDDatatype.XSDstring))
  }

  def createObservationTriples(obs: Observation, model: Model, id: Int) = {
    val obsResource = model.createResource(PrefixObs + "obs" + /*obs.indicator.id + "-" + obs.area.iso2Code + "-" + obs.year.toString + "-" + obs.status*/ id)
    obsResource.addProperty(PropertyRdfType,
      ResourceFactory.createResource(PrefixWiOnto + "Observation"))
    obsResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWiOrg + "WESO"))
    obsResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    obsResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWiOrg + "WebFoundation"))
    obsResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "Observation"))
    obsResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(obs.indicator.label + " in " + obs.area.iso3Code +
      " during " + obs.year, "en"))
    obsResource.addProperty(PropertyWiOntoRefarea, ResourceFactory.createResource(PrefixCountry + obs.area.iso3Code))
    obsResource.addProperty(PropertyWiOntoRefcomputation, ResourceFactory.createResource(PrefixCex + obs.status))
    obsResource.addProperty(PropertyCexIndicator, ResourceFactory.createResource(PrefixIndicator + obs.indicator.id.replace(" ", "_")))
    obsResource.addProperty(PropertyWiOntoRefYear, ResourceFactory.createTypedLiteral(
      String.valueOf(obs.year), XSDDatatype.XSDinteger))
    obsResource.addProperty(PropertyCexValue, ResourceFactory.createTypedLiteral(
      String.valueOf(obs.value), XSDDatatype.XSDdouble))
    obsResource.addProperty(PropertySmdxObsStatus, ResourceFactory.createResource(PrefixCex + obs.status))
    obsResource.addProperty(PropertyQbDataset, ResourceFactory.createResource(PrefixDataset + obs.dataset.id.replace(" ", "_")))
    obsResource.addProperty(PropertyWiOntoShetType, ResourceFactory.createResource(PrefixWiOnto + obs.status))
    obsResource.addProperty(PropertyCexMD5, ResourceFactory.createLangLiteral(
      "MD5 checksum for observation " + id, "en"))
  }

  def createSecondaryIndicatorTriples(indicator: Indicator, model: Model) = {
    val indicatorResource = model.createResource(PrefixIndicator + indicator.id.replace(" ", "_"))
    indicatorResource.addProperty(PropertyCexMD5,
      ResourceFactory.createLangLiteral("MD5 checksum for indicator " + indicator.id, "en"))
    indicatorResource.addProperty(PropertyCexComponent,
      ResourceFactory.createResource(PrefixComponent + indicator.component.id.replace(" ", "_")))
    indicatorResource.addProperty(PropertyCexHighLow, ResourceFactory.createResource(PrefixCex + indicator.highLow))
    indicatorResource.addProperty(PropertyDcTermsSource, indicator.source)
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Indicator"))
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWiOnto + indicator.indicatorType + "Indicator"))
    indicatorResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(indicator.label, "en"))
    indicatorResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PropertyTimeStarts, ResourceFactory.createTypedLiteral("2009", XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PropertyTimeFinishes, ResourceFactory.createTypedLiteral("2012", XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PropertyWiOntoCountryCoverage, ResourceFactory.createTypedLiteral(indicator.countriesCoverage.toString, XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PropertyWiOntoProviderLink, ResourceFactory.createResource(PrefixWiOrg + "WESO"))
    indicatorResource.addProperty(PropertyWiOntoRefSource, ResourceFactory.createResource(PrefixWiOrg + "WESO"))
    createIndicatorWeightTriples(indicator, model)
  }

  def createPrimaryIndicatorTriples(indicator: Indicator, model: Model) = {
    val indicatorResource = model.createResource(PrefixIndicator + indicator.id.replace(" ", "_"))
    indicatorResource.addProperty(PropertyCexMD5,
      ResourceFactory.createLangLiteral("MD5 checksum for indicator " + indicator.id, "en"))
    indicatorResource.addProperty(PropertyCexComponent,
      ResourceFactory.createResource(PrefixComponent + indicator.component.id.replace(" ", "_")))
    indicatorResource.addProperty(PropertyCexHighLow, ResourceFactory.createResource(PrefixCex + indicator.highLow))
    indicatorResource.addProperty(PropertyDcTermsSource, indicator.source)
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Indicator"))
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWiOnto + indicator.indicatorType + "Indicator"))
    indicatorResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(indicator.label, "en"))
    indicatorResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PropertySkosNotation, ResourceFactory.createTypedLiteral(indicator.id, XSDDatatype.XSDstring))
    indicatorResource.addProperty(PropertySkosDefinition, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PropertyTimeStarts, ResourceFactory.createTypedLiteral("2011", XSDDatatype.XSDint))
    indicatorResource.addProperty(PropertyTimeFinishes, ResourceFactory.createTypedLiteral("2011", XSDDatatype.XSDint))
    indicatorResource.addProperty(PropertyWiOntoRefSource, ResourceFactory.createResource(PrefixWiOrg + "WESO"))
    createIndicatorWeightTriples(indicator, model)
  }

  def createIndicatorWeightTriples(indicator: Indicator, model: Model) = {
    val weightResource = model.getResource(PrefixWeightSchema + "indicatorWeights")
    weightResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "WeightSchema"))
    val anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Weight"))
    anonymousResource.addProperty(PropertyCexElement, ResourceFactory.createResource(PrefixIndicator + indicator.id.replace(" ", "_")))
    anonymousResource.addProperty(PropertyCexValue, ResourceFactory.createTypedLiteral(indicator.weight.toString, XSDDatatype.XSDdouble))
    weightResource.addProperty(PropertyCexWeight, anonymousResource)
  }

  def createComponentsTriples(component: Component, model: Model) = {
    val componentResource = model.createResource(PrefixComponent + component.id.replace(" ", "_"))
    componentResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Component"))
    componentResource.addProperty(PropertyCexMD5, ResourceFactory.createLangLiteral("MD5 for" + component.name, "en"))
    componentResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWiOrg + "WESO"))
    componentResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    componentResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWiOrg + "WebFoundation"))
    componentResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(component.name, "en"))
    componentResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    component.getIndicators.foreach(indicator => {
      componentResource.addProperty(PropertyCexElement, ResourceFactory.createResource(PrefixIndicator + indicator.id.replace(" ", "_")))
    })

    val weightComponent = model.createResource(PrefixWeightSchema + "componentWeights")
    weightComponent.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "WeightSchema"))
    val anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Weight"))
    anonymousResource.addProperty(PropertyCexElement, ResourceFactory.createResource(PrefixComponent + component.id.replace(" ", "_")))
    anonymousResource.addProperty(PropertyCexValue, ResourceFactory.createTypedLiteral(component.weight.toString, XSDDatatype.XSDdouble))
    weightComponent.addProperty(PropertyCexWeight, anonymousResource)
  }

  def createSubindexTriples(subindex: SubIndex, model: Model) {
    val subindexResource = model.createResource(PrefixSubindex + subindex.id.replace(" ", "_"))
    subindexResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "SubIndex"))
    subindexResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(subindex.name, "en"))
    subindexResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(subindex.description, "en"))
    subindex.getComponents.foreach(component => {
      subindexResource.addProperty(PropertyCexElement, ResourceFactory.createResource(PrefixComponent + component.id.replace(" ", "_")))
    })

    val weightSubindex = model.createResource(PrefixWeightSchema + "subindexWeights")
    weightSubindex.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex))
    val anonymousResource = model.createResource
    anonymousResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Weight"))
    anonymousResource.addProperty(PropertyCexElement, ResourceFactory.createResource(PrefixSubindex + subindex.id.replace(" ", "_")))
    anonymousResource.addProperty(PropertyCexValue, ResourceFactory.createTypedLiteral(subindex.weight.toString, XSDDatatype.XSDdouble))
    weightSubindex.addProperty(PropertyCexWeight, anonymousResource)
  }

  private def createDatasetsTriples(dataset: Dataset,
    observationsByDataset: Map[Dataset, ListBuffer[Observation]],
    model: Model) = {
    val datasetResource = model.createResource(PrefixDataset + dataset.id.replace(" ", "_"))
    datasetResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "DataSet"))
    datasetResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWiOnto + "Dataset"))
    datasetResource.addProperty(PropertyCexMD5, ResourceFactory.createTypedLiteral("MD5...", XSDDatatype.XSDstring))
    datasetResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWiOrg + "WESO"))
    datasetResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    datasetResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWiOrg + "WebFoundation"))
    datasetResource.addProperty(PropertyDcTermsTitle, ResourceFactory.createLangLiteral(dataset.id, "en"))
    datasetResource.addProperty(PropertyDcTermsSubject, ResourceFactory.createResource(PrefixSdmxSubject + "2.5"))
    datasetResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(dataset.id, "en"))
    datasetResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral("Description of dataset " + dataset.id, "en"))
    datasetResource.addProperty(PropertySMDXUnitMeasure, ResourceFactory.createResource("http://dbpedia.org/resource/Year"))
    var anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWiOnto + "ref-area"))
    anonymousResource.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWiOnto + "ref-area"))
    anonymousResource.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWiOnto + "ref-area"))
    anonymousResource.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("3", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbMeasure, ResourceFactory.createResource(PrefixCex + "indicator"))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    datasetResource.addProperty(PropertyQbStructure, ResourceFactory.createResource(PrefixWiOnto + "DSD"))

    observationsByDataset.get(dataset) match {
      case Some(observations) =>
        val observationsByYear: Map[Int, ListBuffer[Observation]] = observations.groupBy(observation => observation.year)
        observationsByYear.keySet.foreach(year => {
          val sliceResource = model.createResource(PrefixSlice + "Slice-" +
            observations.head.indicator.id.replace(" ", "_") + year.toString + "-" + observations.head.status)
          sliceResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "Slice"))
          sliceResource.addProperty(PropertyCexIndicator, ResourceFactory.createResource(PrefixIndicator +
            observations.head.indicator.id.replace(" ", "_")))
          sliceResource.addProperty(PropertyWiOntoRefYear, ResourceFactory.createTypedLiteral(year.toString, XSDDatatype.XSDinteger))
          sliceResource.addProperty(PropertyQbSliceStructure, /*datasetResource*/ ResourceFactory.createResource(PrefixWiOnto +
            "sliceByArea"))
          observationsByYear.get(year).getOrElse(throw new IllegalArgumentException).foreach(obs => {
            createObservationTriples(obs, model, id)
            sliceResource.addProperty(PropertyQbObservation, ResourceFactory.createResource(PrefixObs + "obs" + id))
            id += 1
          })
          datasetResource.addProperty(PropertyQbSlice, sliceResource)
        })
      case None => IssueManagerUtils.addError(message = s"No observations for the dataset ${dataset.id}", path = Some("RAW File"))
    }
  }

  private def createModel: com.hp.hpl.jena.rdf.model.Model = {
    val model = ModelFactory.createDefaultModel
    model.setNsPrefix("obs", PrefixObs)
    model.setNsPrefix("wi-onto", PrefixWiOnto)
    model.setNsPrefix("dcterms", PrefixDcTerms)
    model.setNsPrefix("wi-org", PrefixWiOrg)
    model.setNsPrefix("qb", PrefixQb)
    model.setNsPrefix("country", PrefixCountry)
    model.setNsPrefix("cex", PrefixCex)
    model.setNsPrefix("indicator", PrefixIndicator)
    model.setNsPrefix("sdmx-concept", PrefixSdmxConcept)
    model.setNsPrefix("sdmx-code", PrefixSdmxCode)
    model.setNsPrefix("dataset", PrefixDataset)
    model.setNsPrefix("rdf", PrefixRdf)
    model.setNsPrefix("rdfs", PrefixRdfs)
    model.setNsPrefix("component", PrefixComponent)
    model.setNsPrefix("subindex", PrefixSubindex)
    model.setNsPrefix("weightSchema", PrefixWeightSchema)
    model.setNsPrefix("slice", PrefixSlice)
    model.setNsPrefix("region", PrefixRegion)
    model.setNsPrefix("skos", PrefixSkos)
    model.setNsPrefix("time", PrefixTime)
    model.setNsPrefix("smdx-attribute", PrefixSdmxAttribute)
    model.setNsPrefix("smdx-subject", PrefixSdmxSubject)
    model
  }
}

object ModelGenerator {

  val PrefixDcTerms = "http://purl.org/dc/terms/"
  val PrefixQb = "http://purl.org/linked-data/cube#"
  val PrefixCex = "http://purl.org/weso/ontology/computex#"
  val PrefixSdmxConcept = "http://purl.org/linked-data/sdmx/2009/concept#"
  val PrefixSdmxCode = "http://purl.org/linked-data/sdmx/2009/code#"
  val PrefixRdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val PrefixRdfs = "http://www.w3.org/2000/01/rdf-schema#"
  val PrefixTime = "http://www.w3.org/2006/time#"
  val PrefixSkos = "http://www.w3.org/2004/02/skos/core#"
  val PrefixSdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#"
  val PrefixSdmxSubject = "http://purl.org/linked-data/sdmx/2009/subject#"

  val PropertyDcTermsPublisher = ResourceFactory.createProperty(PrefixDcTerms
    + "publisher")
  val PropertyDcTermsContributor = ResourceFactory.createProperty(PrefixDcTerms
    + "contributor")
  val PropertyDcTermsSource = ResourceFactory.createProperty(PrefixDcTerms + "source")
  val PropertyDcTermsIssued = ResourceFactory.createProperty(PrefixDcTerms + "issued")
  //val PropertyDcTermsCreated = ResourceFactory.createProperty(PREFIX_DCTERMS + "created")
  val PropertyDcTermsTitle = ResourceFactory.createProperty(PrefixDcTerms + "title")
  val PropertyDcTermsSubject = ResourceFactory.createProperty(PrefixDcTerms + "subject")

  val PropertyQbDataset = ResourceFactory.createProperty(PrefixQb
    + "dataSet")
  val PropertyQbDimension = ResourceFactory.createProperty(PrefixQb + "dimension")
  val PropertyQbOrder = ResourceFactory.createProperty(PrefixQb + "order")
  val PropertyQbMeasure = ResourceFactory.createProperty(PrefixQb + "measure")
  val PropertyQbComponent = ResourceFactory.createProperty(PrefixQb + "component")
  val PropertyQbSliceStructure = ResourceFactory.createProperty(PrefixQb + "sliceStructure")
  val PropertyQbObservation = ResourceFactory.createProperty(PrefixQb + "observation")
  val PropertyQbAttribute = ResourceFactory.createProperty(PrefixQb + "attribute")
  val PropertyQbComponentRequired = ResourceFactory.createProperty(PrefixQb + "componentRequired")
  val PropertyQbComponentAttachment = ResourceFactory.createProperty(PrefixQb + "componentAttachment")
  val PropertyQbSliceKey = ResourceFactory.createProperty(PrefixQb + "sliceKey")
  val PropertyQbComponentProperty = ResourceFactory.createProperty(PrefixQb + "componentProperty")
  val PropertyQbStructure = ResourceFactory.createProperty(PrefixQb + "structure")
  val PropertyQbSlice = ResourceFactory.createProperty(PrefixQb + "slice")

  val PropertyCexIndicator = ResourceFactory.createProperty(PrefixCex
    + "indicator")
  val PropertyCexValue = ResourceFactory.createProperty(PrefixCex
    + "value")
  val PropertyCexMD5 = ResourceFactory.createProperty(PrefixCex + "md5-checksum")
  val PropertyCexComponent = ResourceFactory.createProperty(PrefixCex + "component")
  val PropertyCexHighLow = ResourceFactory.createProperty(PrefixCex + "highLow")
  val PropertyCexElement = ResourceFactory.createProperty(PrefixCex + "element")
  val PropertyCexWeight = ResourceFactory.createProperty(PrefixCex + "weight")

  val PropertySmdxObsStatus = ResourceFactory.createProperty(PrefixSdmxConcept
    + "obsStatus")

  val PropertyRdfType = ResourceFactory.createProperty(PrefixRdf + "type")

  val PropertyRdfsLabel = ResourceFactory.createProperty(PrefixRdfs + "label")
  val PropertyRdfsComment = ResourceFactory.createProperty(PrefixRdfs + "comment")

  val PropertyTimeStarts = ResourceFactory.createProperty(PrefixTime + "intervalStarts")
  val PropertyTimeFinishes = ResourceFactory.createProperty(PrefixTime + "intervalFinishes")

  val PropertySkosNotation = ResourceFactory.createProperty(PrefixSkos + "notation")
  val PropertySkosDefinition = ResourceFactory.createProperty(PrefixSkos + "definition")
  val PropertySkosHasComponent = ResourceFactory.createProperty(PrefixSkos + "has-component")

  val PropertySMDXUnitMeasure = ResourceFactory.createProperty(PrefixSdmxAttribute + "unitMeasure")

}
