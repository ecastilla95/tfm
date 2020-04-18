package commons

import java.io.{BufferedWriter, File, FileWriter}

import exception.FileDeletionException

object FileUtils {

  /**
    * Writes/overwrites a file
    * @param path path of the file
    * @param fileName name of the file
    * @param text text to write in the empty file / overwrite in the existing file
    */
  def writeFile(path: String, fileName: String, text: String): Unit = writeFile(path, fileName, Array(text))

  /**
    * Same as above but for an array of strings as text
    */
  def writeFile(path: String, fileName: String, text: Array[String]): Unit = writeFile(path + fileName, text)

  /**
   * Same as above but the path and file name are already concatenated
   */
  def writeFile(path: String, text: Array[String]): Unit = {
    val bw = new BufferedWriter(new FileWriter(new File(path)))
    text.foreach(bw.write)
    bw.close()
  }

  /**
    * Deletes the given File if it's a file
    * Deletes all content including the folder if it's a folder
    * @param file File to delete
    */
  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory) file.listFiles.foreach(deleteRecursively)
    if (file.exists && !file.delete) throw new FileDeletionException(file.getAbsolutePath)
  }
}

