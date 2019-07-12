package pocs.ibm

import com.ibm.cloud.sdk.core.security.basicauth.BasicAuthConfig
import com.ibm.cloud.sdk.core.service.security.IamOptions
import com.ibm.watson.language_translator.v3.LanguageTranslator
import com.ibm.watson.language_translator.v3.model.TranslateOptions
import com.ibm.watson.language_translator.v3.util.Language

object WatsonTranslator extends App {
  val iamOptions = new IamOptions.Builder()
    .apiKey("") //Execute with API key as argument
    .build()

  val basic = new BasicAuthConfig.Builder()
    .username("")
    .password("")
    .build()

  val service = new LanguageTranslator("2019-07-03", iamOptions)

  translate("Hola mundo")

  def translate(text: String): Unit = {
    val translateOptions = new TranslateOptions.Builder()
      .addText(text)
      .source(Language.SPANISH)
      .target(Language.ENGLISH)
      .build()

    val translationResult = service.translate(translateOptions).execute().getResult

    println(translationResult)
  }
}
