package es.weso.wiFetcher.generator
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.ResourceFactory
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
import es.weso.wiFetcher.persistence.VirtuosoLoader
import java.text.SimpleDateFormat
import com.hp.hpl.jena.rdf.model.Resource
import es.weso.wiFetcher.entities.Provider
import com.hp.hpl.jena.rdf.model.ResourceFactory
import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.libs.ws.Response
import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.functional.syntax._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

case class ModelGenerator(baseUri: String, namespace : String, year : String)(implicit val sFetcher: SpreadsheetsFetcher) {
  import ModelGenerator._
 
  val md5 = java.security.MessageDigest.getInstance("MD5")
  
  val PrefixBase = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/").toString
  val PrefixObs = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/observation/").toString
  val PrefixWfOnto = new StringBuilder(baseUri).append("/ontology/").toString
  val PrefixWfOrg = new StringBuilder(baseUri).append("/organization/").toString
  val PrefixCountry = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/country/").toString
  val PrefixRegion = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/region/").toString
  val PrefixIndicator = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/indicator/").toString
  val PrefixDataset = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/dataset/").toString
  val PrefixComponent = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/component/").toString
  val PrefixSubindex = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/subindex/").toString
  val PrefixIndex = new StringBuilder(baseUri).append("/").append(namespace)
  	.append("/").append(year).append("/index/").toString
  val PrefixWeightSchema = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/weightSchema/").toString
  val PrefixSlice = new StringBuilder(baseUri).append("/")
  	.append(namespace).append("/").append(year).append("/slice/").toString
  val PrefixWfPeople = new StringBuilder(baseUri).append("/people/").toString

  val PropertyWiOntoRefarea = ResourceFactory.createProperty(PrefixWfOnto
    + "ref-area")
  val PropertyWfOntoHasCountry = ResourceFactory.createProperty(PrefixWfOnto + "has-country")
  val PropertyWfOntoRefcomputation = ResourceFactory.createProperty(PrefixWfOnto
    + "ref-computation")
  val PropertyWfOntoRefYear = ResourceFactory.createProperty(PrefixWfOnto
    + "ref-year")
  val PropertyWfOntoSheetType = ResourceFactory.createProperty(PrefixWfOnto
    + "sheet-type")
  val PropertyWfOntoCountryCoverage = ResourceFactory.createProperty(PrefixWfOnto + "country-coverage")
  val PropertyWfOntoProviderLink = ResourceFactory.createProperty(PrefixWfOnto + "provider-link")
  val PropertyWfOntoRefSource = ResourceFactory.createProperty(PrefixWfOnto + "ref-source")
  val PropertyWfOntoISO2 = ResourceFactory.createProperty(PrefixWfOnto + "has-iso-alpha2-code")
  val PropertyWfOntoISO3 = ResourceFactory.createProperty(PrefixWfOnto + "has-iso-alpha3-code")
  val PropertyWfOntoRefSourceData = ResourceFactory.createProperty(PrefixWfOnto + "ref-source-data")
  
  private var id: Int = 1

  def generateJenaModel(spreadsheetsFetcher: SpreadsheetsFetcher/*, store: Boolean*/, timestamp : Long, imp: Option[String] = None): String = {
    //val observations : List[Observation] = SpreadsheetsFetcher.observations.toList
    val observationsByDataset = spreadsheetsFetcher.observations.groupBy(
      observation => observation.dataset)
    val model = createModel
    createDatasetMetadata(model, timestamp)
    createDataStructureDefinition(model)
    createComputationFlow(model)
    val indexResource = model.createResource(PrefixIndex + "index")
    indexResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Index"))
    indexResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral("The Index", "en"))
    spreadsheetsFetcher.primaryIndicators.toList.foreach(
      indicator => createPrimaryIndicatorTriples(indicator, model))
    spreadsheetsFetcher.secondaryIndicators.toList.foreach(
      indicator => createSecondaryIndicatorTriples(indicator, model))
    spreadsheetsFetcher.components.foreach(
      comp => createComponentsTriples(comp, model))
    spreadsheetsFetcher.subIndexes.foreach(
      subindex => createSubindexTriples(subindex, indexResource, model))
    spreadsheetsFetcher.datasets.foreach(
      dataset => {
        if(!dataset.id.contains("Missed")) 
        	createDatasetsTriples(dataset, observationsByDataset, model)
      })  
    spreadsheetsFetcher.countries.foreach(
      country => createCountriesTriples(country, model))
    spreadsheetsFetcher.regions.foreach(
      region => createRegionsTriples(region, model))
    spreadsheetsFetcher.providers.foreach(
		provider => createProviderTriples(provider, model)
    )
      
    val path = saveModel(model, timestamp)
      
    generateUploadScript(path, timestamp)

    path
  }
  
  private def createDatasetMetadata(model : Model, timestamp : Long) = {
    val datasetResource = model.createResource(PrefixBase + "ds")
    datasetResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixVoid + "Dataset"))
    datasetResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral("Web Foundation | Web Index", "en"))
    datasetResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral("Dataset of all raw information about Web Index 2013", "en"))
    datasetResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWfOrg + "WESO"))
    datasetResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWfOrg + "WebFoundation"))
    val format = new SimpleDateFormat("yyyy-MM-dd")
    datasetResource.addProperty(PropertyDcTermsCreated, ResourceFactory.createTypedLiteral(format.format(timestamp), XSDDatatype.XSDdate))
    datasetResource.addProperty(PropertyDcTermsLicense, ResourceFactory.createResource("http://opendatacommons.org/licenses/by/1.0/"))
    datasetResource.addProperty(ResourceFactory.createProperty(PrefixFoaf + "homepage"), ResourceFactory.createResource("http://data.webfoundation.org"))
  }
  
  private def createComputationFlow(model : Model) = {
    val computation = model.createResource(PrefixComputation + "Flow")
    computation.addProperty(PropertyRdfType, 
        ResourceFactory.createResource(PrefixCex + "ComputationFlow"))
    val steps = model.createSeq()
    val copyRaw = model.createResource()
    copyRaw.addProperty(PropertyCexQuery, "copyRaw")
    val meanBetweenMissing = model.createResource()
    meanBetweenMissing.addProperty(PropertyCexQuery, "MeanBetweenMissing")
    val avgGrowth2Missing = model.createResource()
    avgGrowth2Missing.addProperty(PropertyCexQuery, "AvgGrowth2Missing")
    val filter = model.createResource
    filter.addProperty(PropertyCexQuery, "Filter")
    val zscores = model.createResource
    zscores.addProperty(PropertyCexQuery, "zScores")
    val adjust = model.createResource
    adjust.addProperty(PropertyCexQuery, "Adjust")
    val weightedSimple = model.createResource
    weightedSimple.addProperty(PropertyCexQuery, "WeightedSimple")
    val groupClusters = model.createResource
    groupClusters.addProperty(PropertyCexQuery, "GroupClusters")
    val groupSubindex = model.createResource
    groupSubindex.addProperty(PropertyCexQuery, "GroupSubindex")
    val weigthedMean = model.createResource
    weigthedMean.addProperty(PropertyCexQuery, "WeightedMean")
    val ranking = model.createResource
    ranking.addProperty(PropertyCexQuery, "Ranking")
    steps.add(copyRaw)
    steps.add(meanBetweenMissing)
    steps.add(avgGrowth2Missing)
    steps.add(filter)
    steps.add(zscores)
    steps.add(adjust)
    steps.add(weightedSimple)
    steps.add(groupClusters)
    steps.add(groupSubindex)
    steps.add(weigthedMean)
    steps.add(ranking)
    computation.addProperty(PropertyCexSteps, steps)
  }

  private def saveModel(model: Model, timestamp : Long): String = {
    val path = s"reports/dataset-${timestamp}.ttl"
    model.write(new FileOutputStream(new File(s"public/${path}")), "TURTLE")
    path
  }

  private def generateUploadScript(path: String, timestamp : Long) = {
    val builder = new StringBuilder(baseUri)
    builder.append("/").append(namespace).append("/").append(year)
    VirtuosoLoader.generateCode(timestamp, path, builder.toString)
  }

  def createDataStructureDefinition(model: Model) = {
    val dsd = model.createResource(PrefixWfOnto + "DSD")
    dsd.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "DataStructureDefinition"))
    var dimension = model.createResource()
    dimension.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWfOnto + "ref-area"))
    dimension.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger))
    dsd.addProperty(PropertyQbComponent, dimension)
    dimension = model.createResource()
    dimension.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWfOnto + "ref-year"))
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
    val sliceByArea = model.createResource(PrefixWfOnto + "sliceByArea")
    dsd.addProperty(PropertyQbSliceKey, sliceByArea)
    sliceByArea.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "SliceKey"))
    sliceByArea.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral("slice by area", "en"))
    sliceByArea.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral("Slice by grouping areas together fixing year", "en"))
    sliceByArea.addProperty(PropertyQbComponentProperty, ResourceFactory.createResource(PrefixCex + "indicator"))
    sliceByArea.addProperty(PropertyQbComponentProperty, ResourceFactory.createResource(PrefixWfOnto + "ref-year"))
  }

  def createRegionsTriples(region: Region, model: Model) = {
    val regionResource = model.createResource(PrefixRegion + region.name.replace("& ", "").replace(" ", "_").replace(".", ""))
    regionResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWfOnto + "Region"))
    regionResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWfOnto + "WESO"))
    regionResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWfOnto + "WebFoundation"))
    regionResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    regionResource.addProperty(PropertyRdfsLabel, ResourceFactory.createTypedLiteral(region.name, XSDDatatype.XSDstring))
    region.getCountries.foreach(country => regionResource.addProperty(PropertyWfOntoHasCountry, ResourceFactory.createResource(PrefixCountry + country.iso3Code)))
  }

  def createCountriesTriples(country: Country, model: Model) = {
    val countryResource = model.createResource(PrefixCountry + country.iso3Code)
    countryResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWfOnto + "Country"))
    //countryResource.addProperty(PropertyCexMD5, ResourceFactory.createTypedLiteral("MD5 for " + country.iso3Code, XSDDatatype.XSDstring))
    countryResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWfOnto + "WESO"))
    countryResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    countryResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWfOrg + "WebFoundation"))
    countryResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(country.name, "en"))
    countryResource.addProperty(PropertyWfOntoISO2, ResourceFactory.createTypedLiteral(country.iso2Code, XSDDatatype.XSDstring))
    countryResource.addProperty(PropertyWfOntoISO3, ResourceFactory.createTypedLiteral(country.iso3Code, XSDDatatype.XSDstring))
  }

  def createObservationTriples(obs: Observation, model: Model, id: Int) = {
    val obsResource = model.createResource(PrefixObs + "obs" + /*obs.indicator.id + "-" + obs.area.iso2Code + "-" + obs.year.toString + "-" + obs.status*/ id)
    obsResource.addProperty(PropertyRdfType,
      ResourceFactory.createResource(PrefixWfOnto + "Observation"))
    obsResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWfOrg + "WESO"))
    obsResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    obsResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWfOrg + "WebFoundation"))
    obsResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "Observation"))
    obsResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(obs.label, "en"))
    obsResource.addProperty(PropertyWiOntoRefarea, ResourceFactory.createResource(PrefixCountry + obs.area.iso3Code))
    obsResource.addProperty(PropertyWfOntoRefcomputation, ResourceFactory.createResource(PrefixCex + obs.status))
    obsResource.addProperty(PropertyCexIndicator, ResourceFactory.createResource(PrefixIndicator + obs.indicator.id.replace(" ", "_")))
    obsResource.addProperty(PropertyWfOntoRefYear, ResourceFactory.createTypedLiteral(
      String.valueOf(obs.year), XSDDatatype.XSDinteger))
    val value = obs.value
    if(!value.isEmpty)
    	obsResource.addProperty(PropertyCexValue, ResourceFactory.createTypedLiteral(
    			String.valueOf(obs.value.get), XSDDatatype.XSDdouble))
    obsResource.addProperty(PropertySmdxObsStatus, ResourceFactory.createResource(PrefixCex + obs.status))
    obsResource.addProperty(PropertyQbDataset, ResourceFactory.createResource(PrefixDataset + obs.dataset.id.replace(" ", "_")))
    obsResource.addProperty(PropertyWfOntoSheetType, ResourceFactory.createResource(PrefixWfOnto + obs.sheet))
    val builder = new StringBuilder
    builder.append(obs.indicator.id).append("#").append(obs.status).append("#")
    	.append(obs.area.iso3Code).append("#").append(obs.year).append("#")
    	.append(obs.value).append("#WESO")
    val md5Value : String = md5.digest(builder.toString.getBytes).map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
    obsResource.addProperty(PropertyCexMD5, 
        ResourceFactory.createPlainLiteral(md5Value))
  }

  def createSecondaryIndicatorTriples(indicator: Indicator, model: Model) = {
    val indicatorResource = model.createResource(PrefixIndicator + indicator.id.replace(" ", "_"))
    /*indicatorResource.addProperty(PropertyCexMD5,
      ResourceFactory.createLangLiteral("MD5 checksum for indicator " + indicator.id, "en"))*/
    indicatorResource.addProperty(PropertyCexComponent,
      ResourceFactory.createResource(PrefixComponent + indicator.component.id.replace(" ", "_")))
    indicatorResource.addProperty(PropertyCexHighLow, ResourceFactory.createResource(PrefixCex + indicator.highLow))
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Indicator"))
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWfOnto + indicator.indicatorType + "Indicator"))
//    indicatorResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(indicator.label, "en"))
//    indicatorResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PropertyTimeStarts, ResourceFactory.createTypedLiteral("2009", XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PropertyTimeFinishes, ResourceFactory.createTypedLiteral("2012", XSDDatatype.XSDinteger))
    indicatorResource.addProperty(PropertyWfOntoCountryCoverage, ResourceFactory.createTypedLiteral(indicator.countriesCoverage.toString, XSDDatatype.XSDinteger))
    indicator.providers.foreach(provider => {
      indicatorResource.addProperty(PropertyWfOntoProviderLink, ResourceFactory.createResource(PrefixWfOrg + provider.id))
    })
    indicatorResource.addProperty(PropertyWfOntoRefSource, ResourceFactory.createResource(indicator.source))
    indicator.labels.keySet.foreach(lang => {
      val label = indicator.labels.get(lang).get
      if(!label.isEmpty)
    	  indicatorResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(label, lang))
    })
    indicator.comments.keySet.foreach(lang => {
      val comment = indicator.comments.get(lang).get
      if(!comment.isEmpty)
        indicatorResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(comment, lang))
        indicatorResource.addProperty(PropertySkosDefinition, ResourceFactory.createLangLiteral(comment, lang))
    })
    createIndicatorWeightTriples(indicator, model)
  }

  def createPrimaryIndicatorTriples(indicator: Indicator, model: Model) = {
    val indicatorResource = model.createResource(PrefixIndicator + indicator.id.replace(" ", "_"))
    /*indicatorResource.addProperty(PropertyCexMD5,
      ResourceFactory.createLangLiteral("MD5 checksum for indicator " + indicator.id, "en"))*/
    indicatorResource.addProperty(PropertyCexComponent,
      ResourceFactory.createResource(PrefixComponent + indicator.component.id.replace(" ", "_")))
    indicatorResource.addProperty(PropertyCexHighLow, ResourceFactory.createResource(PrefixCex + indicator.highLow))
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Indicator"))
    indicatorResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWfOnto + indicator.indicatorType + "Indicator"))
//    indicatorResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(indicator.label, "en"))
//    indicatorResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PropertySkosNotation, ResourceFactory.createTypedLiteral(indicator.id, XSDDatatype.XSDstring))
//    indicatorResource.addProperty(PropertySkosDefinition, ResourceFactory.createLangLiteral(indicator.comment, "en"))
    indicatorResource.addProperty(PropertyTimeStarts, ResourceFactory.createTypedLiteral("2011", XSDDatatype.XSDint))
    indicatorResource.addProperty(PropertyTimeFinishes, ResourceFactory.createTypedLiteral("2011", XSDDatatype.XSDint))
    indicator.providers.foreach(provider => {
      indicatorResource.addProperty(PropertyWfOntoProviderLink, ResourceFactory.createResource(PrefixWfOrg + provider.id))
    })
    indicatorResource.addProperty(PropertyWfOntoRefSource, ResourceFactory.createResource(indicator.source))
    indicator.labels.keySet.foreach(lang => {
      val label = indicator.labels.get(lang).get
      if(!label.isEmpty)
    	  indicatorResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(label, lang))
    })
    indicator.comments.keySet.foreach(lang => {
      val comment = indicator.comments.get(lang).get
      if(!comment.isEmpty)
        indicatorResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(comment, lang))
        indicatorResource.addProperty(PropertySkosDefinition, ResourceFactory.createLangLiteral(comment, lang))
    })
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
    /*componentResource.addProperty(PropertyCexMD5, ResourceFactory.createLangLiteral("MD5 for" + component.names.get("en").get, "en"))*/
    componentResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWfOrg + "WESO"))
    componentResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    componentResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWfOrg + "WebFoundation"))
//    componentResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(component.name, "en"))
    component.names.keySet.foreach(lang => {
      val label = component.names.get(lang).get
      if(!label.isEmpty)
    	  componentResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(label, lang))
    })
    component.descriptions.keySet.foreach(lang => {
      val comment = component.descriptions.get(lang).get
      if(!comment.isEmpty)
    	  componentResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(comment, lang))      
    })
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

  def createSubindexTriples(subindex: SubIndex, indexResource : Resource, model: Model) {
    val subindexResource = model.createResource(PrefixSubindex + subindex.id.replace(" ", "_"))
    subindexResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "SubIndex"))
//    subindexResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(subindex.name, "en"))
//    subindexResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(subindex.description, "en"))
    subindex.names.keySet.foreach(lang => {
      val label = subindex.names.get(lang).get
      if(!label.isEmpty)
    	  subindexResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(label, lang))
    })
    subindex.descriptions.keySet.foreach(lang => {
      val comment = subindex.descriptions.get(lang).get
      if(!comment.isEmpty)
    	  subindexResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral(comment, lang))      
    })
    
    subindex.getComponents.foreach(component => {
      subindexResource.addProperty(PropertyCexElement, ResourceFactory.createResource(PrefixComponent + component.id.replace(" ", "_")))
    })
    indexResource.addProperty(PropertyCexElement, subindexResource)

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
    if(dataset.id.contains("Ordered")) {
      val computationResource = model.createResource()
      computationResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Raw"))
      datasetResource.addProperty(PropertyCexComputation, computationResource)
    } else if(dataset.id.contains("Imputed")) {
      val computationResource = model.createResource()
      computationResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixCex + "Imputed"))
      datasetResource.addProperty(PropertyCexComputation, computationResource)
    }
    datasetResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "DataSet"))
    datasetResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixWfOnto + "Dataset"))
    /*datasetResource.addProperty(PropertyCexMD5, ResourceFactory.createTypedLiteral("MD5...", XSDDatatype.XSDstring))*/
    datasetResource.addProperty(PropertyDcTermsContributor, ResourceFactory.createResource(PrefixWfOrg + "WESO"))
    datasetResource.addProperty(PropertyDcTermsIssued, ResourceFactory.createTypedLiteral(DateUtils.getCurrentTimeAsString, XSDDatatype.XSDdate))
    datasetResource.addProperty(PropertyDcTermsPublisher, ResourceFactory.createResource(PrefixWfOrg + "WebFoundation"))
    datasetResource.addProperty(PropertyDcTermsTitle, ResourceFactory.createLangLiteral(dataset.id, "en"))
    datasetResource.addProperty(PropertyDcTermsSubject, ResourceFactory.createResource(PrefixSdmxSubject + "2.5"))
    datasetResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(dataset.id, "en"))
    datasetResource.addProperty(PropertyRdfsComment, ResourceFactory.createLangLiteral("Description of dataset " + dataset.id, "en"))
    datasetResource.addProperty(PropertySMDXUnitMeasure, ResourceFactory.createResource("http://dbpedia.org/resource/Year"))
    var anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWfOnto + "ref-area"))
    anonymousResource.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWfOnto + "ref-area"))
    anonymousResource.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbDimension, ResourceFactory.createResource(PrefixWfOnto + "ref-area"))
    anonymousResource.addProperty(PropertyQbOrder, ResourceFactory.createTypedLiteral("3", XSDDatatype.XSDinteger))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    anonymousResource = model.createResource()
    anonymousResource.addProperty(PropertyQbMeasure, ResourceFactory.createResource(PrefixCex + "indicator"))
    datasetResource.addProperty(PropertyQbComponent, anonymousResource)
    datasetResource.addProperty(PropertyQbStructure, ResourceFactory.createResource(PrefixWfOnto + "DSD"))

    observationsByDataset.get(dataset) match {
      case Some(observations) =>
        val observationsByYear: Map[Int, ListBuffer[Observation]] = observations.groupBy(observation => observation.year)
        observationsByYear.keySet.foreach(year => {
          val sliceResource = model.createResource(PrefixSlice + "Slice-" +
            observations.head.indicator.id.replace(" ", "_") + year.toString + "-" + observations.head.sheet)
          sliceResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixQb + "Slice"))
          sliceResource.addProperty(PropertyCexIndicator, ResourceFactory.createResource(PrefixIndicator +
            observations.head.indicator.id.replace(" ", "_")))
          sliceResource.addProperty(PropertyWfOntoRefYear, ResourceFactory.createTypedLiteral(year.toString, XSDDatatype.XSDinteger))
          sliceResource.addProperty(PropertyQbSliceStructure, ResourceFactory.createResource(PrefixWfOnto +
            "sliceByArea"))
          observationsByYear.get(year).getOrElse(throw new IllegalArgumentException).foreach(obs => {
            createObservationTriples(obs, model, id)
            sliceResource.addProperty(PropertyQbObservation, ResourceFactory.createResource(PrefixObs + "obs" + id))
            id += 1
          })
          datasetResource.addProperty(PropertyQbSlice, sliceResource)
        })
      case None => sFetcher.issueManager.addWarn(message = s"No observations for the dataset ${dataset.id}", path = Some("RAW File"))
    }
  }
  
  private def createProviderTriples(provider : Provider, model : Model) = {
    val providerResource = model.createResource(PrefixWfOrg + provider.id)
    providerResource.addProperty(PropertyRdfType, ResourceFactory.createResource(PrefixOrg + "Organization"))
    providerResource.addProperty(PropertyRdfsLabel, ResourceFactory.createLangLiteral(provider.name, "en"))
    providerResource.addProperty(PropertyOrgIdentifier, ResourceFactory.createTypedLiteral(provider.id, XSDDatatype.XSDstring))
    providerResource.addProperty(PropertyFoafHomepage, ResourceFactory.createResource(provider.web))
    providerResource.addProperty(PropertyWfOntoRefSourceData, ResourceFactory.createResource(provider.source))    
  }

  private def createModel: com.hp.hpl.jena.rdf.model.Model = {
    val model = ModelFactory.createDefaultModel
    addPrefixes(model)
    model
  }
  
  
  
  private def addPrefixes(model : Model) = {
    val response : Future[Response] = WS.url("http://156.35.82.103:9003/prefixes/json").get
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    val future = response.map{
      response =>
        val prefixes = response.json.as[Map[String, String]]
        prefixes.keySet.foreach(prefix => {
          model.setNsPrefix(prefix, prefixes.get(prefix).get)
        })
    }
    Await.result(future, 5 minutes)
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
  val PrefixComputation = "http://data.webfoundation.org/webindex/v2013/computation/"
  val PrefixVoid = "http://rdfs.org/ns/void#"
  val PrefixFoaf = "http://xmlns.com/foaf/0.1/"
  val PrefixOrg = "http://www.w3.org/ns/org#"

  val PropertyDcTermsPublisher = ResourceFactory.createProperty(PrefixDcTerms
    + "publisher")
  val PropertyDcTermsContributor = ResourceFactory.createProperty(PrefixDcTerms
    + "contributor")
  val PropertyDcTermsSource = ResourceFactory.createProperty(PrefixDcTerms + "source")
  val PropertyDcTermsIssued = ResourceFactory.createProperty(PrefixDcTerms + "issued")
  val PropertyDcTermsCreated = ResourceFactory.createProperty(PrefixDcTerms + "created")
  val PropertyDcTermsTitle = ResourceFactory.createProperty(PrefixDcTerms + "title")
  val PropertyDcTermsSubject = ResourceFactory.createProperty(PrefixDcTerms + "subject")
  val PropertyDcTermsLicense = ResourceFactory.createProperty(PrefixDcTerms + "license")

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
  val PropertyCexSteps = ResourceFactory.createProperty(PrefixCex + "steps")
  val PropertyCexQuery = ResourceFactory.createProperty(PrefixCex + "query")
  val PropertyCexComputation = ResourceFactory.createProperty(PrefixCex + "computation")

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
  
  val PropertyFoafHomepage = ResourceFactory.createProperty(PrefixFoaf + "homepage")
  
  val PropertyOrgIdentifier = ResourceFactory.createProperty(PrefixOrg + "identifier")

}