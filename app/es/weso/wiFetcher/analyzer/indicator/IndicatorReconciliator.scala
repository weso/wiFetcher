package es.weso.wiFetcher.analyzer.indicator

import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.index.DirectoryReader
import es.weso.wiFetcher.entities.Indicator
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy
import org.apache.lucene.index.IndexDeletionPolicy
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.document.Document
import org.apache.lucene.document.TextField
import org.apache.lucene.document.Field
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.Query
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.ScoreDoc
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher

/**
 * This class contains the implementation of a web index indicator reconciliator
 * This class using Apache Lucene to index a list of indicators, and search 
 * over them given a string
 */
class IndicatorReconciliator {
  //The directory where the reconciliator indexes the indicators. In this case
  //is a RAM directory
  private val idx : RAMDirectory = new RAMDirectory
   
  //The name of the field of the Lucene document in which the id of an indicator 
  //is stored
  private val INDICATOR_ID_FIELD = "id"
  //The name of the field of the Lucene document in which the name of an 
  //indicator is stored
  private val INDICATOR_NAME_FIELD = "name"
  //The number of maximum results that have to returns the reconciliator
  private val MAX_RESULTS = 1 
  //An analyzer that contains some filters over search strings
  private val analyzer : IndicatorAnalyzer = new IndicatorAnalyzer
  
  /**
   * This method has to index a list of indicators in a directory of Apache 
   * Lucene
   * @param indicators A list of indicators that the reconciliator has to index
   */
  def indexIndicators(indicators : List[Indicator]) = {
    val deletionPolicy : IndexDeletionPolicy = 
    		new KeepOnlyLastCommitDeletionPolicy
	val indexConfiguration : IndexWriterConfig = new IndexWriterConfig(
	    Version.LUCENE_40, analyzer)
    indexConfiguration.setIndexDeletionPolicy(deletionPolicy)
    //Create the index writer
    val indexWriter : IndexWriter = new IndexWriter(idx, indexConfiguration) 
    //For each indicator, create a Lucene document with the data that we want to
    //index. In this case we store the identifier and the name of a indicator.
    //Once it is created, we index in the directory
    indicators.foreach(indicator =>{
      val doc : Document = new Document
      val id : TextField = new TextField(INDICATOR_ID_FIELD, indicator.id, 
          Field.Store.YES)
      val name : TextField = new TextField(INDICATOR_NAME_FIELD, 
          indicator.label, Field.Store.YES)
      doc.add(id)
      doc.add(name)
      indexWriter.addDocument(doc)
    })
    //Finish, close the index writer to avoid IO problems
    indexWriter.close()
  }
  
  /**
   * This method has to create a Lucene simple query given a string. The query
   * is build to search only in the field "name"
   * @param str The search query
   * @return The lucene query built
   */
  private def createQueryFromString(str : String) : Query = {
    var queryStr = str.replace("/", " ")
    val parser : QueryParser = new QueryParser(Version.LUCENE_40,
        INDICATOR_NAME_FIELD, analyzer)
    parser.setDefaultOperator(QueryParser.Operator.OR)
    parser.parse(queryStr)
  }
  
  /**
   * This method has to find an indicator given it's name. Given an indicator
   * name, it has to build the query and search over Lucene directory in order
   * to find the indicator
   * @param indicator The indicator name
   * @return The indicator searched
   */
  def searchIndicator(indicator : String) : Indicator = {
    //Create the index searcher
    val indexSearcher : IndexSearcher = new IndexSearcher(
      DirectoryReader.open(idx))
    val collector = TopScoreDocCollector.create(MAX_RESULTS, true)
    //Build the query
    val query = createQueryFromString(indicator)
    //Search over the directory
    indexSearcher.search(query, collector)
    //Process the result
    val scoreDocs : Array[ScoreDoc] = collector.topDocs().scoreDocs
    if(scoreDocs.size == 0) {
       null
     } else {
       val doc : Document = indexSearcher.doc(scoreDocs.head.doc)
	   val id = doc.getField(INDICATOR_ID_FIELD).stringValue()
	   SpreadsheetsFetcher.obtainIndicatorById(id)
     }
  }

}