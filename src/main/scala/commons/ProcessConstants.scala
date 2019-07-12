package commons

object ProcessConstants {

  // Where the data folder is located in the machine where the project is run
  val DATA_FOLDER: String = ProcessInfo.get("data.folder").toString

  // Name and location of the Ibex35 historical data file
  val IBEX35_HISTORICAL_FILE: String = DATA_FOLDER + ProcessInfo.get("data.ibex35.file").toString

}
