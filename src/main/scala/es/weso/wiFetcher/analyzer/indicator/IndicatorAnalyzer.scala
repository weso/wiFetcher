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
  
	/**
	 * Create some filters to apply over search strings
	 */
	def createComponents(arg0 : String, reader : Reader) : 
		TokenStreamComponents = {
	    //create a standard analyzer of Lucene
		var result : Tokenizer = new StandardTokenizer(Version.LUCENE_40, reader) 
		//create a lower case filter
    	var resultStream : TokenStream = new LowerCaseFilter(Version.LUCENE_40, 
    	    result)
		//create a trim filter
		resultStream = tf.create(result)
		//create a word delimiter filter
		resultStream = df.create(result)
		//Create a stop words filter
		resultStream = new StopFilter(Version.LUCENE_40, result, 
		    createStopWordsSet)
		new TokenStreamComponents(result, resultStream)
   	}
	
	/**
	 * This method has to create a set of stop words that have to been 
	 * removed of the search string. This words are available in a resource file
	 */
	def createStopWordsSet() : CharArraySet = {	
		val m_Words : HashSet[String] = new HashSet[String]
		//Read the file that contains the words
		val src = Source.fromFile(FileUtils.getFilePath(
				Configuration.getIndicatorStopWordsFile, true), "UTF-8")
		//Each line of the file is a stop word
		src.getLines.foreach(str => {
		  m_Words.add(str)
		})
		CharArraySet.copy(Version.LUCENE_40, m_Words)
	}

}