package pocs.indico

import java.{lang, util}

import io.indico.Indico
import io.indico.api.results.{BatchIndicoResult, IndicoResult}

object IndicoBasicExample extends App {
  val indico = new Indico("80254518ef53f692986b174af794ae88")
  val single: IndicoResult = indico.sentiment.predict(
    "I love writing code!"
  )

  val result: Double = single.getSentiment
  println(result)

  val example: Array[String] = Array(
    "I love writing code!",
    "Alexander and the Terrible, Horrible, No Good, Very Bad Day"
  )

  val multiple: BatchIndicoResult = indico.sentiment.predict(example)
  val results: util.List[lang.Double] = multiple.getSentiment
  println(results)
}