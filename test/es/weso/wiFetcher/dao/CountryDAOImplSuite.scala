package es.weso.wiFetcher.dao

import java.io.FileNotFoundException
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import es.weso.wiFetcher.dao.file.CountryDAOImpl

@RunWith(classOf[JUnitRunner])
class CountryDAOImplSuite extends FunSuite with BeforeAndAfter 
	with Matchers{

  test("Try to load countries specifying a null path") {
    intercept[IllegalArgumentException]{
      new CountryDAOImpl(null, true)(null)
    }
  }
  
  test("Try to load countries from a non-existing file") {
    intercept[FileNotFoundException] {
      new CountryDAOImpl("test.txt", true)(null)
    }
  }
  
  test("Load correct file an verify that all data is loaded") {
    val countryDao = new CountryDAOImpl("files/countryCodes.tsv", true)(null)
    val countries = countryDao.getCountries
    countries.size should be (236)
    countries.foreach(country => {
      if(country.name == null || country.name.equals("") || 
          country.iso2Code == null || country.iso2Code.equals("") || 
          country.iso3Code == null || country.iso3Code.equals(""))
        throw new Exception("There is a country without important " +
        		"information")
    })
    val c = countries.find(country => country.name.equals("Spain")).getOrElse(
        throw new Exception("Country spain is not present in the list"))
    c.iso2Code should be ("ES")
    c.iso3Code should be ("ESP")
  }
}