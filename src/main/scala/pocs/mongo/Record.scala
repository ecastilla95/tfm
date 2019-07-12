package pocs.mongo

import org.mongodb.scala.bson.ObjectId

case class Record(_id: ObjectId, date: String, content: String)


