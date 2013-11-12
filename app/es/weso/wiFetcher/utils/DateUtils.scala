package es.weso.wiFetcher.utils

import java.util.Calendar
import java.text.SimpleDateFormat 


object DateUtils {
  /**
   * This method obtains the current time, and format it to yyyy-mm-dd in a
   * string
   */
  def getCurrentTimeAsString(): String = {
    val actual = Calendar.getInstance().getTime()

    val yearFormat = new SimpleDateFormat("yyyy")
    val monthFormat = new SimpleDateFormat("MM")
    val dayFormat = new SimpleDateFormat("dd")
    val currentYear = yearFormat.format(actual)
    val currentMonth = monthFormat.format(actual)
    val currentDay = dayFormat.format(actual)
    new StringBuilder(currentYear).append("-").append(currentMonth)
      .append("-").append(currentDay).toString
  } 

}