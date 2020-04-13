package spark

import org.apache.log4j.Logger
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkUtils {

  val logger: Logger = Logger.getLogger(this.getClass)

  /**
    * Creates a Spark Session
    * @param name name of the spark session
    * @return spark session
    */
  def createSparkSession(name: String): SparkSession = {
    logger.info("Creating a new Spark Session")
    val spark = SparkSession.builder()
//      .appName(name)
      .master("local[*]") // TODO Delete this line if executed as Spark Submit
      .config("spark.io.compression.codec", "snappy")
      .getOrCreate()
    logger.info("Spark Session created successfully")
    spark.sparkContext.setLogLevel("WARN")
    spark
  }

  /**
    * Reads a dataframe from a CSV file
    * @param spark ongoing spark session
    * @param path where the file file is located
    * @return dataframe of the CSV
    */
  def readCSV(spark: SparkSession, path: String): DataFrame = {
    logger.info("Loading CSV from: " + path)
    val csv = spark.read
      .format("csv")
      .option("header", "true")
      .option("delimiter", ",")
      .load(path)
    logger.info("CSV loaded successfully")
    csv
  }

  /**
    * Reads a dataframe from a CSV file
    * @param spark ongoing spark session
    * @param path where the file file is located
    * @param schema data schema
    * @return dataframe of the CSV
    */
  def readCSVWithSchema(spark: SparkSession, path: String, schema: StructType): DataFrame = {
    logger.info("Loading CSV from: " + path)
    val csv = spark.read
      .format("csv")
      .option("header", "false")
      .schema(schema)
      .load(path)
    logger.info("CSV loaded successfully")
    csv
  }
}
