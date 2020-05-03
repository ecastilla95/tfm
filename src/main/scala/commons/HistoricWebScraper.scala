package commons

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{DomNode, HtmlPage}
import org.joda.time.Days
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.util
import scala.util.Try

/**
 * Historic web scraper for origin ElPais
 */
object HistoricWebScraper extends App {

  // Date Time Format
  val dateTimeFormat = DateTimeFormat.forPattern("yyyyMMdd")

  // Array of dates
  val truncYear: Int => DateTime = n => new DateTime(n, 1, 1, 0, 0, 0, 0)
  val from = new DateTime(2020, 1, 1, 0, 0, 0, 0)
  val until = new DateTime(2020, 5, 1, 0, 0, 0, 0)
  val numberOfDays = Days.daysBetween(from, until).getDays
  val days: Seq[DateTime] = for (f <- 0 to numberOfDays) yield from.plusDays(f)

  // Here we construct a web client and set CSS and JS support to false
  val webClient = new WebClient()
  webClient.getOptions.setCssEnabled(false)
  webClient.getOptions.setJavaScriptEnabled(false)

  // We do the web scraping process for each day
  days.foreach(scrapDate)

  // We try to read the page given a string URL and we write it on the given folder path with today's date as name
  // TODO: clean up
  def scrapDate(dt: DateTime): Unit = {
    val year = "%04d".format(dt.getYear)
    val month = "%02d".format(dt.getMonthOfYear)
    val day = "%02d".format(dt.getDayOfMonth)

    val urls = Seq("m", "t", "n").map(x => s"https://elpais.com/hemeroteca/elpais/$year/$month/$day/$x/portada.html")
    val path = """src/main/data/news/historical/elpais/""" + dateTimeFormat.print(dt) + ".txt"

    val result = urls.flatMap(url => Try {
      val page = webClient.getPage(url).asInstanceOf[HtmlPage]

      // We find all the news headlines:
      var listOfNews: Seq[String] = Seq()

      val selector1: String = page.querySelectorAll(".articulo--primero > div:nth-child(1) > h2:nth-child(2)").get(0)
        .getTextContent.split("\n").map(_.trim.filter(_ >= ' ')).mkString
      listOfNews = listOfNews :+ selector1
      val selector2: String = page.querySelectorAll("#bloque_actualidad_destacadas > div:nth-child(1) > div:nth-child(1) > div:nth-child(1) > article:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)").get(0)
        .getTextContent.trim
      listOfNews = listOfNews :+ selector2

      for(i <- Range(0, 10)) { //TODO hardcoded range but it is always <4 anyways
        val selector: Int => String = n => s"#bloque_actualidad_cuerpo > div:nth-child(1) > div:nth-child($n) > div:nth-child(1) > article:nth-child($i) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)"
        val querySelector: Int => util.List[DomNode] = x => page.querySelectorAll(selector(x))
        Option(querySelector(3)).filter(!_.isEmpty).map(_.get(0).asText().trim).foreach(e => listOfNews = listOfNews :+ e)
        Option(querySelector(4)).filter(!_.isEmpty).map(_.get(0).asText().trim).foreach(e => listOfNews = listOfNews :+ e)
      }
      listOfNews
    }.toOption).flatten.distinct.map(x => x + "\n")

    if(result.nonEmpty) {
      FileUtils.writeFile(path, result.toArray)
    }
  }
}


