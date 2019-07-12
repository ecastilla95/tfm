package pocs.mongo

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

class MyMongoDatabase(name: String){

  // Codec creation for Document <-> Record Case Class equivalency
  val codecRegistry = fromRegistries(fromProviders(classOf[Record]), DEFAULT_CODEC_REGISTRY )

  // Directly connect to the default server localhost on port 27017 and use database "news"
  val mongoClient: MongoClient = MongoClient()
  val db: MongoDatabase = mongoClient.getDatabase(name).withCodecRegistry(codecRegistry)

  // Definition of Completed in order to subscribe operations over the database successfully
  val observerCompleted: Observer[Completed] = new Observer[Completed]{
    override def onNext(result: Completed): Unit = println("Inserted successfully")
    override def onError(e: Throwable): Unit = println("Failed to insert document")
    override def onComplete(): Unit = println("Task completed")
  }

  // Implementation of database methods
  def getCollection(collection: String): MongoCollection[Record] = db.getCollection(collection)

  def insertOne(collection: MongoCollection[Record], record: Record): Unit = collection.insertOne(record).subscribe(observerCompleted)
  def insertOne(collection: String, record: Record): Unit = insertOne(getCollection(collection), record)

  def insertMany(collection: MongoCollection[Record], records: Seq[Record]): Unit = collection.insertMany(records).subscribe(observerCompleted)
  def insertMany(collection: String, records: Seq[Record]): Unit = insertMany(getCollection(collection), records)

}


