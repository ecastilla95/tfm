package commons

object ProcessConstants {

  // Where the data folder is located in the machine where the project is run
  val DATA_FOLDER: String = ProcessInfo.get("data.folder").toString

  // Name and location of the Ibex35 historical data file
  val IBEX35_HISTORICAL_FILE: String = DATA_FOLDER + ProcessInfo.get("data.ibex35.file").toString
  val IBEX35_HISTORICAL_NEW_FILE: String = DATA_FOLDER + ProcessInfo.get("data.ibex35.newfile").toString

  val HADOOP_HOME: String = ProcessInfo.get("data.hadoop.home").toString

  // What to do when the Ibex35 is on the raise
  val BULL_MARKET_DECISION = "SELL"

}
