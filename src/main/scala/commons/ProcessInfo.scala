package commons

import java.io.File
import java.util.{Calendar, Date}

import com.typesafe.config.ConfigFactory
import exception.InvalidConfigurationException

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * This object reads the application.conf file and parses it as a Map
  * It also provides methods for accessing said Map values
  */
object ProcessInfo {

  // TODO: point this to the proper place and/or dinamically
  private lazy val config = ConfigFactory.parseFile(new File("/home/bigdata/IdeaProjects/tfm/src/main/resources/application.conf"))

  // This is the map that stores the configuration values from the application.conf file
  // TODO: review how this works
  private val configMap = Map(config.getConfig("process").entrySet().asScala.map(x => {
    x.getKey -> x.getValue
  }).toSeq: _*)

  // Retrieves a value given it's key
  def get(key: String): AnyRef = {
    Try[AnyRef] {
      configMap(key).unwrapped()
    } match {
      case Success(s) => s
      case Failure(f) => throw InvalidConfigurationException(s"Unable to get configuration value for key $key in configuration file.")
    }
  }

}
