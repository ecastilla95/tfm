package spark.v2

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import commons.{FileUtils, ProcessConstants}
import org.apache.spark.sql.functions.{max, min, when}
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}
import spark.{Ibex35, NormalisedWeights, SparkUtils}

/**
  * After doing the sentiment analysis part in Python, we read the results we wrote as a dataframe
  * and we match the information with the Ibex35 historical data, once this is done we write the
  * resulting dataframe in the local file system.
  */
object CalculateMainDf extends App {

  // TODO Windows only
  System.setProperty("hadoop.home.dir", ProcessConstants.HADOOP_HOME)

  // Creation of a Spark session
  val spark = SparkUtils.createSparkSession("CalculateMainDf")
  import spark.sqlContext.implicits._

  // Schema used to read the dataframe stored in several files
  val schema = new StructType()
    .add("name", StringType, nullable = true)
    .add("weight", DoubleType, nullable = true)

  // Directory of the results of the sentiment analysis process
  val dir = new File(ProcessConstants.DATA_FOLDER + "historical.news\\")

  // We read the files in such directory
  val files: Array[String] = dir.listFiles().map(_.getAbsolutePath)

  // We read the CSV data stored in such files and we pile the results together
  val data = files.map(SparkUtils.readCSVWithSchema(spark, _, schema)).reduce(_ union _)

  // Calculation of the minimum and maximum weights in the dataframe
  val stats = data.select(min($"weight"), max($"weight")).first()
  val minWeight = stats.getAs[Double](0)
  val maxWeight = stats.getAs[Double](1)

  // Transformation of the dataframe into more meaningful data
  val news = data.map { row =>

    // We read the existing rows
    val origin = "elpais"
    val name = row.getAs[String]("name")
    val weight = row.getAs[Double]("weight")

    // We extract the date from the name of the file with a regular expression
    val date = name.replaceAll("\\D+", "")
    // Calculation of the normalised weight for a given day
    val normalised = (weight - minWeight) / (maxWeight - minWeight)

    NormalisedWeights(origin, date, normalised)
  }.as("news")


  // Date formats required to parse and format the date in the Ibex35 historical file
  val locale = new Locale("en")
  val entryFormat = new SimpleDateFormat("MMM dd, yyyy", locale)
  val exitFormat = new SimpleDateFormat("yyyyMMdd", locale)

  // Reading from the local file system the historical file
  val ibex = SparkUtils.readCSV(spark, ProcessConstants.IBEX35_HISTORICAL_NEW_FILE)
      .map{ row =>
        // We transform the date into the yyyyMMdd format
        val date = exitFormat.format(entryFormat.parse(row.getAs[String]("Date")))
        // We parse the string containing the change percentage into a Double
        val change = row.getAs[String]("Change %").trim.replace("%", "").toDouble
        Ibex35(date, change)
      }.as("ibex")

  // This join creates a dataframe where all the collected data is registered
  val df = news.join(ibex, $"news.date" === $"ibex.date", "full_outer")
    .select(
      when($"news.date".isNotNull, $"news.date").otherwise($"ibex.date").as("date"),
      $"news.origin".as("origin"),
      $"news.weight".as("weight"),
      $"ibex.change".as("change")
    ).coalesce(1)

  // We check the folder in the local file system where we are going to write our dataframe and delete its contents if needed
  val writeDir = new File(ProcessConstants.DATA_FOLDER + "mainDf2020\\")
  FileUtils.deleteRecursively(writeDir)

  val path = "src\\main\\data\\mainDf2020"
  // We format the dataframe as a CSV file and save it as a text file
  df.write.format("csv").save(path)

}
