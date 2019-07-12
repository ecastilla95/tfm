package commons

import java.text.SimpleDateFormat
import java.util.Calendar

object TimeUtils {

  def getPeriod(period: Int): String => Int = {
    date =>
      val format = new SimpleDateFormat("yyyyMMdd")
      val calendar = Calendar.getInstance()
      calendar.setTime(format.parse(date))
      calendar.get(period)
  }

  // This function receives a date in format yyyyMMdd and returns the day of the year associated to that date
  val getDay: String => Int = getPeriod(Calendar.DAY_OF_YEAR)

  // This function receives a date in format yyyyMMdd and returns the week of the year containing that date
  val getWeek: String => Int = getPeriod(Calendar.WEEK_OF_YEAR)


}

