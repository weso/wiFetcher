package es.weso.wiFetcher.dao

import es.weso.wiFetcher.entities.Observation
import es.weso.wiFetcher.entities.Dataset
import es.weso.wiFetcher.entities.ObservationStatus._
import es.weso.reconciliator.CountryReconciliator
import es.weso.wiFetcher.configuration.Configuration
import es.weso.wiFetcher.fetchers.SpreadsheetsFetcher
import es.weso.wiFetcher.entities.Country
import es.weso.wiFetcher.entities.ObservationStatus
import es.weso.wiFetcher.entities.Computation
import es.weso.wiFetcher.entities.Indicator
import es.weso.wiFetcher.entities.Area

trait ObservationDAO  extends DAO[Observation]{

  val reconciliator: CountryReconciliator = new CountryReconciliator(
    Configuration.getCountryReconciliatorFile, true)
  
    /**
   * This method has to extract the name of the country corresponding to the
   * observation.
   * @param sheet The sheet that contains all observation of a dataset
   * @param dataset A dataset corresponds to the excel sheet from we have to
   * extract the country name
   * @param column The column corresponds to an observation
   * @param row The row corresponds to an observation
   * @param initialCell The initial cell of the observations
   * @return A country corresponds to an observations
   */
  def obtainCountry(countryName: String)
  	(implicit sFetcher : SpreadsheetsFetcher): Option[Country] = {
    //Ask to SpreadsheetFetcher for the country accord to the Web Index name
    val country = sFetcher.obtainCountry(countryName)
    country match {
      case Some(c) => ""
      case None => "foo"
    }
    country
  }
  
  /**
   * This method has to create an observation
   * @param dataset The dataset corresponds to an observation
   * @param label
   * @param area The area that refers the observation
   * @param computation
   * @param indicator The indicator measured at the observations
   * @param year The year of the observation
   * @param value The value of the observation
   * @param status The status of the observation
   */
  def createObservation(dataset: Dataset, label: String, area: Area,
    computation: Computation, indicator: Indicator, year: Double,
    value: Double, status: String, XslxFile : Option[String])(implicit sFetcher : SpreadsheetsFetcher): Observation = {

    val tmpStatus = if (value == -1)
      ObservationStatus.Missed
    else status match {
      case "Raw" => ObservationStatus.Raw
      case "Imputed" => ObservationStatus.Imputed
      case "Normalised" => ObservationStatus.Normalised
      case "Missed" => ObservationStatus.Missed
      case "Sorted" => ObservationStatus.Sorted
      case "Adjusted" => ObservationStatus.Adjusted
      case "Weighted" => ObservationStatus.Weighted
      case "Ordered" => ObservationStatus.Ordered
      case _ =>
        sFetcher.issueManager.addError(message = "Observation status " +
          status + " is unknown", path = XslxFile)
        ObservationStatus.Wrong
    }

    Observation(
      dataset,
      label,
      area,
      computation,
      indicator,
      year.toInt,
      value,
      tmpStatus)

  }

}