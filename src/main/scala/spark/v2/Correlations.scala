package spark.v2

import commons.{ProcessConstants, Report, TimeUtils}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}
import spark.SparkUtils

/**
  * This object showcases a statistical study of the relation between the weights of the sentiment analysis process
  * and the change in the Ibex35 indicator.
  */
object Correlations extends App {

  // Creation of a Spark session
  val spark = SparkUtils.createSparkSession("Correlations")
  import spark.sqlContext.implicits._

  // Schema used to read the main dataframe
  val schema = new StructType()
    .add("date", StringType, nullable = true)
    .add("origin", StringType, nullable = true)
    .add("weight", DoubleType, nullable = true)
    .add("change", DoubleType, nullable = true)

  // We read the main dataframe
  val df = SparkUtils.readCSVWithSchema(spark, ProcessConstants.DATA_FOLDER + "mainDf/", schema)

  /**
    * We are going to try calculating a series of metrics on our dataframe to study
    * the available data and try to extract any meaningful conclusion
    */

  val report = new Report
  import report._

  /**
    * We start with the simplest calculation we can think of, we remove all the incomplete rows
    * and calculate the correlation between the remaining values.
    */
  def calculateCorrelation(df: DataFrame): String = df.select(corr("weight", "change")).first().mkString("")
  val simpleCorrelation = calculateCorrelation(df.na.drop())
  addStatement("The value of the simple correlation is: " + simpleCorrelation)

  /**
    * We try another simple calculation, this time without removing the null values.
    */

  val completeCorrelation = calculateCorrelation(df)
  addStatement("The value of the complete correlation is: " + completeCorrelation)

  /**
    * We observe that these two values are the same, therefore internally the null values have been removed
    * Now we will try to generate a weekly aggregation of data
    */

  def getWeekUDF: UserDefinedFunction = udf(TimeUtils.getWeek)

  def getWeeklyDf(df: DataFrame): DataFrame = df.withColumn("week", getWeekUDF($"date")).groupBy("week")
    .agg(avg("weight"), avg("change"))

  def calculateWeeklyCorrelation(weeklyDf: DataFrame) = weeklyDf
    .select(corr("avg(weight)", "avg(change)"))
    .first().mkString("")

  val weeklyDf = getWeeklyDf(df)
  val weeklyCorrelation = calculateWeeklyCorrelation(weeklyDf)
  addStatement("The value of the weekly correlation is: " + weeklyCorrelation)

  /**
    * Now we are going to ask ourselves more complicated questions
    * First of all, ¿how does the change in the Ibex35 indicator correlate with next day's news?
    */

  def getDayUDF: UserDefinedFunction = udf(TimeUtils.getDay)

  def calculateNumberedCorrelation(df: DataFrame) = {
    val numberedDays = df.withColumn("day", getDayUDF($"date"))
    val numberedDaysIbex = numberedDays.drop("date", "origin", "weight").as("ibex")
    val numberedDaysWeights = numberedDays.drop("date", "change").as("weights")

    def nDaysDisplacementCorrelation(n: Int) = numberedDaysIbex
      .join(numberedDaysWeights, $"ibex.day" === ($"weights.day" + n), "full_outer")
      .select(corr("weight", "change")).first().mkString("")

    (nDaysDisplacementCorrelation(1), nDaysDisplacementCorrelation(-1), nDaysDisplacementCorrelation(2))
  }

  val (numberedDaysCorrelation, oppositeNumberedDaysCorrelation, twoDaysCorrelation) = calculateNumberedCorrelation(df)
  addStatement("The correlation between one day's news sentiment rating and next days Ibex35 change is " + numberedDaysCorrelation)

  /**
    * Similarly, ¿how does the change in the sentiment in national news affect next day's Ibex35 indicator?
    */

  addStatement("The correlation between one day's Ibex35 rating and next days news sentiment is " + oppositeNumberedDaysCorrelation)

  /**
    * In addition, ¿how does the change in the sentiment in national news affect the Ibex35 indicator in two days time?
    */

  addStatement("The correlation between one day's news sentiment rating and the Ibex35 indicator in two days time is " + twoDaysCorrelation)

  /**
    * Similarly, we answer the same questions for weekly data
    */

  def calculateNumberedWeeklyCorrelation(weeklyDf: DataFrame) = weeklyDf.as("_1").join(weeklyDf.as("_2"), $"_1.week" === ($"_2.week" + 1))
    .select(corr("_1.avg(weight)", "_2.avg(change)"), corr("_2.avg(weight)", "_1.avg(change)")).first()

  val numberedDaysJoined = calculateNumberedWeeklyCorrelation(weeklyDf)
  addStatement("The correlation between one week's Ibex35 change and next weeks news sentiment rating is " + numberedDaysJoined.get(0))
  addStatement("The correlation between one week's news sentiment rating and next weeks Ibex35 change is "+ numberedDaysJoined.get(1))

  /**
    * Now, we have been looking at the average of the news sentiment between our three sources, ¿what happens if we study each one of them individually?
    * ¿is there a source that is more accurate when predicting Ibex35 ratings? Let's find out:
    */

  def reportCorrelationsFromOrigin(origin: String): Unit = {
    val filteredDf = df.filter(($"origin" === lit(origin)) or $"origin".isNull)

    val correlation = calculateCorrelation(filteredDf)
    addStatement(s"$origin: The value of the correlation for the origin $origin is: $correlation")

    val weeklyFilteredDf = getWeeklyDf(filteredDf)
    val weeklyFilteredCorrelation = calculateWeeklyCorrelation(weeklyFilteredDf)
    addStatement(s"$origin: The value of the weekly correlation for the origin $origin is: $weeklyFilteredCorrelation")

    val (numberedDaysFilteredCorrelation, oppositeNumberedDaysFilteredCorrelation, twoDaysFilteredCorrelation) = calculateNumberedCorrelation(filteredDf)
    addStatement(s"$origin: The correlation between one day's news sentiment rating and next days Ibex35 change for the origin $origin is $numberedDaysFilteredCorrelation")
    addStatement(s"$origin: The correlation between one day's Ibex35 rating and next days news sentiment for the origin $origin is $oppositeNumberedDaysFilteredCorrelation")
    addStatement(s"$origin: The correlation between one day's news sentiment rating and the Ibex35 indicator in two days time for the origin $origin is $twoDaysFilteredCorrelation")

    val numberedDaysJoinedFiltered = calculateNumberedWeeklyCorrelation(weeklyFilteredDf)

    addStatement(s"$origin: The correlation between one week's Ibex35 change and next weeks news sentiment rating for the origin $origin is ${numberedDaysJoinedFiltered.get(0)}")
    addStatement(s"$origin: The correlation between one week's news sentiment rating and next weeks Ibex35 change for the origin $origin is ${numberedDaysJoinedFiltered.get(1)}")

  }

  Seq("elmundo", "elpais", "expansion").foreach(reportCorrelationsFromOrigin)

  printReport()
}
