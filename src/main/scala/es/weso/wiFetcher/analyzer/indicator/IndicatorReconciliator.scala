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

class IndicatorReconciliator {
  val idx : RAMDirectory = new RAMDirectory
   
  private val INDICATOR_ID_FIELD = "id"
  private val INDICATOR_NAME_FIELD = "name"
  private val MAX_RESULTS = 1 
  private val analyzer : IndicatorAnalyzer = new IndicatorAnalyzer
  
  def indexIndicators(indicators : List[Indicator]) = {
    val deletionPolicy : IndexDeletionPolicy = 
    		new KeepOnlyLastCommitDeletionPolicy
	val indexConfiguration : IndexWriterConfig = new IndexWriterConfig(
	    Version.LUCENE_40, analyzer)
    indexConfiguration.setIndexDeletionPolicy(deletionPolicy)
    val indexWriter : IndexWriter = new IndexWriter(idx, indexConfiguration) 
    indicators.foreach(indicator =>{
      val doc : Document = new Document
      val id : TextField = new TextField(INDICATOR_ID_FIELD, indicator.id, 
          Field.Store.YES)
      val description : TextField = new TextField(INDICATOR_NAME_FIELD, 
          indicator.label, Field.Store.YES)
      doc.add(id)
      doc.add(description)
      indexWriter.addDocument(doc)
    })
    indexWriter.close()
  }
  
  private def createQueryFromString(str : String) : Query = {
    var queryStr = str.replace("/", " ")
    val parser : QueryParser = new QueryParser(Version.LUCENE_40,
        INDICATOR_NAME_FIELD, analyzer)
    parser.setDefaultOperator(QueryParser.Operator.OR)
    parser.parse(queryStr)
  }
  
  def searchIndicator(indicator : String) : Indicator = {
    val indexSearcher : IndexSearcher = new IndexSearcher(
      DirectoryReader.open(idx))
    val collector = TopScoreDocCollector.create(MAX_RESULTS, true)
    val query = createQueryFromString(indicator)
    indexSearcher.search(query, collector)
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