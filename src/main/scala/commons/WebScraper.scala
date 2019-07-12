package commons

import java.text.SimpleDateFormat
import java.util.Calendar

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage

/**
  * This object reads the main page of the given online sources and proceeds to collect all the text
  * contained in the XML describing the webpage
  */
object WebScraper extends App {

  // Date for time-stamping purposes
  val format = new SimpleDateFormat("yyyyMMdd")
  val todayDate = format.format(Calendar.getInstance.getTime)

  //TODO: insert parameter
  val elMundoURL = """https://www.elmundo.es/economia.html"""
  val elPaisURL = """https://elpais.com/economia/"""
  val expansionURL = """http://www.expansion.com/economia.html"""

  //TODO: insert parameter
  val elMundoPath = """src/main/data/news/elmundo/"""
  val elPaisPath = """src/main/data/news/elpais/"""
  val expansionPath = """src/main/data/news/expansion/"""

  // Here we construct a web client and set CSS and JS support to false
  val webClient = new WebClient()
  webClient.getOptions.setCssEnabled(false)
  webClient.getOptions.setJavaScriptEnabled(false)

  // We try to read the page given a string URL and we write it on the given folder path with today's date as name
  // TODO: refactor functionally
  def readXml(url: String, path: String): Unit = {
    try {
      val page: HtmlPage = webClient.getPage(url)
      FileUtils.writeFile(path, todayDate + ".txt", page.asText())
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  readXml(elMundoURL, elMundoPath)
  readXml(elPaisURL, elPaisPath)
  readXml(expansionURL, expansionPath)

}


