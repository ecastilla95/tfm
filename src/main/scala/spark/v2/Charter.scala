package spark.v2

import commons.{FileUtils, ProcessConstants, TimeUtils}
import commons.ProcessConstants.BULL_MARKET_DECISION
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}
import spark.SparkUtils

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
  val df = SparkUtils.readCSVWithSchema(spark, ProcessConstants.DATA_FOLDER + "mainDf2020/", schema)
    .withColumn("date", to_date(col("date"), "yyyyMMdd"))

  val dailyDF = df.groupBy("date").agg(
    avg("weight").as("indicator"), round(avg("change"), 2).as("change"))

  dailyDF.show()

  //////////////////////////////////////
  // Basic same-day prediction
  //////////////////////////////////////
  /*
  val sameDayPrediction = dailyDF.where(($"change".isNotNull) && ($"weight".isNotNull))
      .withColumn("indicator", when($"indicator" >= 0.5, "BUY").otherwise("SELL"))
      .withColumn("correct",
        when($"change" >= 0,
          when($"indicator" === BULL_MARKET_DECISION, true).otherwise(false)
        ).when($"change" < 0,
          when($"indicator" =!= BULL_MARKET_DECISION, true).otherwise(false)
        )
      )

  val sameDayPredictionAccuracy = {
    val correctPredictions = sameDayPrediction.where($"correct" === true).count()
    val totalPredictions = sameDayPrediction.count()
    correctPredictions.toDouble / totalPredictions
  }

  println("Basic same-day prediction accuracy: " + sameDayPredictionAccuracy)
  */
  //////////////////////////////////////
  // Basic next-day prediction
  //////////////////////////////////////
  /*
  val nextDayPrediction = dailyDF.as("_1").join(dailyDF.as("_2"), $"_1.date" === date_add($"_2.date", -1), "full_outer")
    .select("_1.date", "_1.indicator", "_2.change")
    .where(($"indicator".isNotNull) && ($"change".isNotNull))
    .withColumn("indicator", when($"indicator" >= 0.5, "BUY").otherwise("SELL"))
    .withColumn("correct",
      when($"change" >= 0,
        when($"indicator" === BULL_MARKET_DECISION, true).otherwise(false)
      ).when($"change" < 0,
        when($"indicator" =!= BULL_MARKET_DECISION, true).otherwise(false)
      )
    )

  nextDayPrediction.show()

  val nextDayPredictionAccuracy = {
    val correctPredictions = nextDayPrediction.where($"correct" === true).count()
    val totalPredictions = nextDayPrediction.count()
    correctPredictions.toDouble / totalPredictions
  }

  println("Basic next-day prediction accuracy: " + nextDayPredictionAccuracy)
  */
  //////////////////////////////////////
  // Basic two-day prediction
  //////////////////////////////////////

  val twoDayPrediction = dailyDF.as("_1").join(dailyDF.as("_2"), $"_1.date" === date_add($"_2.date", -1), "full_outer")
    .selectExpr("_1.date", "_1.indicator", "round(((1 + (_1.change / 100)) * (1 + (_2.change / 100)) - 1) * 100, 2) as change")
    .where(($"indicator".isNotNull) && ($"change".isNotNull))
    .withColumn("indicator", when($"indicator" >= 0.5, "BUY").otherwise("SELL"))
    .withColumn("correct",
      when($"change" >= 0,
        when($"indicator" === BULL_MARKET_DECISION, true).otherwise(false)
      ).when($"change" < 0,
        when($"indicator" =!= BULL_MARKET_DECISION, true).otherwise(false)
      )
    )

  twoDayPrediction.show()

  val nextDayPredictionAccuracy = {
    val correctPredictions = twoDayPrediction.where($"correct" === true).count()
    val totalPredictions = twoDayPrediction.count()
    correctPredictions.toDouble / totalPredictions
  }

  println("Basic two-day prediction accuracy: " + nextDayPredictionAccuracy)

  //  // Function to filter the main dataframe by origin
//  def filter(origin: String): DataFrame = df.filter(($"origin" === lit(origin)) or $"origin".isNull)
//
//  // The correlation between one week's Ibex35 change and next weeks news sentiment rating is 0.4242741511569938
//  val nextDay = df.as("_1").join(df.as("_2"), $"_1.date" === ($"_2.date" + 1))
//    .select(
//      concat($"_2.week", lit("-"), $"_1.week").as("weeks"),
//      $"_1.avg(weight)".as("next_weeks_weight"),
//      $"_2.avg(change)".as("change")
//    )

}