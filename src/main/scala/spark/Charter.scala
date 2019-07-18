package spark

import commons.{FileUtils, ProcessConstants, TimeUtils}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}

/**
  * This object creates small CSV files for later graphing
  */
object Charter extends App {

  // We create a Spark Session
  val spark = SparkUtils.createSparkSession("Correlations")

  import spark.sqlContext.implicits._

  // This is the schema of the data in the main dataframe
  val schema = new StructType()
    .add("date", StringType, nullable = true)
    .add("origin", StringType, nullable = true)
    .add("weight", DoubleType, nullable = true)
    .add("change", DoubleType, nullable = true)

  // We read the main dataframe with the provided schema
  val df = SparkUtils.readCSVWithSchema(spark, ProcessConstants.DATA_FOLDER + "mainDf/", schema)

  // Function to filter the main dataframe by origin
  def filter(origin: String): DataFrame = df.filter(($"origin" === lit(origin)) or $"origin".isNull)

  def getWeekUDF = udf(TimeUtils.getWeek)

  // Calculate the average weights and change of the Ibex35 indicator in a weekly basis
  def getWeeklyDf(df: DataFrame) = df.withColumn("week", getWeekUDF($"date")).groupBy("week")
    .agg(avg("weight"), avg("change"))

  val weeklyDf = getWeeklyDf(df)

  // The correlation between one week's Ibex35 change and next weeks news sentiment rating is 0.4242741511569938
  val weeklyDisplacedDf = weeklyDf.as("_1").join(weeklyDf.as("_2"), $"_1.week" === ($"_2.week" + 1))
    .select(
      concat($"_2.week", lit("-"), $"_1.week").as("weeks"),
      $"_1.avg(weight)".as("next_weeks_weight"),
      $"_2.avg(change)".as("change")
    )

  val elmundoDf = filter("elmundo")

  // elmundo: The value of the weekly correlation for the origin elmundo is: 0.3067985963486926
  val elmundoWeeklyDf = getWeeklyDf(elmundoDf)

  // elmundo: The correlation between one week's news sentiment rating and next weeks Ibex35 change for the origin elmundo is -0.2968145749768088
  val elmundoWeeklyDisplacedDf = elmundoWeeklyDf.as("_1").join(elmundoWeeklyDf.as("_2"), $"_1.week" === ($"_2.week" + 1))
    .select(
      concat($"_2.week", lit("-"), $"_1.week").as("weeks"),
      $"_2.avg(weight)".as("weight"),
      $"_1.avg(change)".as("next_weeks_change")
    )

  val elpaisDf = filter("elpais")

  // elpais: The value of the weekly correlation for the origin elpais is: -0.320556887620226
  val elpaisWeeklyDf = getWeeklyDf(elpaisDf)

  // elpais: The correlation between one week's Ibex35 change and next week's news sentiment rating for the origin elpais is 0.5623213962093411
  val elpaisWeeklyDisplacedDf = elpaisWeeklyDf.as("_1").join(elpaisWeeklyDf.as("_2"), $"_1.week" === ($"_2.week" + 1))
    .select(
      concat($"_2.week", lit("-"), $"_1.week").as("weeks"),
      $"_1.avg(weight)".as("next_weeks_weight"),
      $"_2.avg(change)".as("change")
    )

  val expansionDf = filter("expansion")
  val expansionWeeklyDf = getWeeklyDf(expansionDf)

  // expansion: The correlation between one week's Ibex35 change and next weeks news sentiment rating for the origin expansion is 0.26579739908148087
  val expansionWeeklyDisplacedDf = expansionWeeklyDf.as("_1").join(expansionWeeklyDf.as("_2"), $"_1.week" === ($"_2.week" + 1))
    .select(
      concat($"_2.week", lit("-"), $"_1.week").as("weeks"),
      $"_1.avg(weight)".as("next_weeks_weight"),
      $"_2.avg(change)".as("change")
    )

  val dfList: Map[String, DataFrame] = Map(
    "weekly_displaced" -> weeklyDisplacedDf,
    "elmundo_weekly" -> elmundoWeeklyDf,
    "elmundo_weekly_displaced" -> elmundoWeeklyDisplacedDf,
    "elpais_weekly" -> elpaisWeeklyDf,
    "elpais_weekly_displaced" -> elpaisWeeklyDisplacedDf,
    "expansion_weekly_displaced" -> expansionWeeklyDisplacedDf
  )

  // This method formats a dataframe as an array of strings equivalent to a CSV with header
  def collectDf(df: DataFrame) = {
    val columns = df.schema.names.mkString(",") + "\n"
    val values = df.map(_.mkString(",") + "\n").collect()
    columns +: values
  }

  // We write the studied files
  dfList.foreach(e => FileUtils.writeFile(ProcessConstants.DATA_FOLDER + "charts/", e._1 + ".csv", collectDf(e._2)))

}