package pocs.mongo

import java.io.File
import java.nio.file.Files

import org.mongodb.scala.bson.ObjectId

object DumpToMongo extends App {

  // Path variables
  val elMundoPath = """src/main/data/news/elmundo/"""
  val elPaisPath = """src/main/data/news/elpais/"""
  val expansionPath = """src/main/data/news/expansion/"""

  // File variables
  val elMundoFolder = new File(elMundoPath)
  val elPaisFolder = new File(elPaisPath)
  val expansionFolder = new File(expansionPath)

  // File lists
  val elMundoFiles: Array[File] = elMundoFolder.list().map(s => new File(elMundoPath + s))
  val elPaisFiles = elPaisFolder.list().map(s => new File(elPaisPath + s))
  val expansionFiles = expansionFolder.list().map(s => new File(expansionPath + s))

  def getRecords(files: Array[File]): Array[Record] = files.map{ x =>
    val content = Files.readAllLines(x.toPath).toArray.mkString(" ")
    val date = x.getName.split('.').head
    Record(new ObjectId(), date, content)
  }

  // Files ready for MongoDB insertion
  val elMundoRecords = getRecords(elMundoFiles).toSeq
  val elPaisRecords = getRecords(elPaisFiles).toSeq
  val expansionRecords = getRecords(expansionFiles).toSeq

  Thread.sleep(100000)
  val mongoDB = new MyMongoDatabase("news")
  Thread.sleep(100000)

  // Insert
  mongoDB.insertMany("elmundo", elMundoRecords)
  mongoDB.insertMany("elpais", elPaisRecords)
  mongoDB.insertMany("expansion", expansionRecords)
}
