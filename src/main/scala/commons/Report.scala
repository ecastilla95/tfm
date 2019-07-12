package commons

/**
  * This report is a tracing mechanism devised in order to print information from a Spark process
  * The main difference between a conventional logging system is that waits until the process has finished
  * before dumping the logs to a file
  */
class Report(private var result: String = "") {

  val separator: String = "-" * 100 + "\n"
  def addSeparator(): Unit = result += separator

  def addLine(line: String): Unit = result += line + "\n"

  def addStatement(line: String): Unit = {
    addSeparator()
    addLine(line)
  }

  def printReport(): Unit = FileUtils.writeFile(ProcessConstants.DATA_FOLDER, "report.txt", result)

}

