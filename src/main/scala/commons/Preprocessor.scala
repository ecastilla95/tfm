package commons

import java.io.File

import scala.io.Source

/**
  * This object reads all folders in the news/ folder and browses them in order to retrieve all web scraping files
  * Once retrieved, these files are read and then filtered by length and content, this is an attempt to increase the accuracy of the sentiment analysis process
  * Finally, the files are written in the preprocessed.news/ folder where they will be read by the Jupyter notebook in charge of said process
  */
object Preprocessor extends App {

  // Folder containing all the data grouped by source subfolder
  val dataFolder = new File(ProcessConstants.DATA_FOLDER + "news/")

  // We iterate through all files in all the subfolders and call the functions below on them
  for (
    folder <- dataFolder.listFiles();
    file <- folder.listFiles()
  ) {
    val contents = preprocess(file)
    val name = folder.getName + "/" + file.getName
    FileUtils.writeFile(ProcessConstants.DATA_FOLDER + "preprocessed.news/", name, contents)
  }

  /**
    * Reads a file, filters the lines in the file by length, removes meaningless lines and maps all characters to lowercase
    * @param file file to be read
    * @return array of strings with filtered file contents
    */
  def preprocess(file: File): Array[String] = {
    val source = Source.fromFile(file)
    val contents = source.getLines().toArray
      .filter{ line => (line.length > 30) && !line.contains("Redacción:") &&  !line.contains("mostrar/ocultar") && !line.contains("EXPANSIÓN")}
      .map(_.toLowerCase + "\n")
    source.close()
    contents
  }
}
