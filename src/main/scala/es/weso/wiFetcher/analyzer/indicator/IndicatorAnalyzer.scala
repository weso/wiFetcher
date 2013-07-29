package es.weso.wiFetcher.analyzer.indicator

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import java.io.Reader
import org.apache.lucene.analysis.miscellaneous.TrimFilterFactory
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.core.LowerCaseFilter
import org.apache.lucene.analysis.core.StopFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.util.CharArraySet
import java.util.HashSet
import scala.io.Source
import es.weso.wiFetcher.utils.FileUtils
import es.weso.wiFetcher.configuration.Configuration

class IndicatorAnalyzer extends Analyzer{
  
	val df : WordDelimiterFilterFactory = new WordDelimiterFilterFactory
	val tf : TrimFilterFactory = new TrimFilterFactory
  
	def createComponents(arg0 : String, reader : Reader) : 
		TokenStreamComponents = {
		var result : Tokenizer = new StandardTokenizer(Version.LUCENE_40, reader)    
    	var resultStream : TokenStream = new LowerCaseFilter(Version.LUCENE_40, 
    	    result)
		resultStream = tf.create(result)
		resultStream = df.create(result)
		resultStream = new StopFilter(Version.LUCENE_40, result, 
		    createStopWordsSet)
		new TokenStreamComponents(result, resultStream)
   	}
	
	def createStopWordsSet() : CharArraySet = {	
		val m_Words : HashSet[String] = new HashSet[String]
		val src = Source.fromFile(FileUtils.getFilePath(
				Configuration.getIndicatorStopWordsFile, true), "UTF-8")
		src.getLines.foreach(str => {
		  m_Words.add(str)
		})
		CharArraySet.copy(Version.LUCENE_40, m_Words)
	}

}