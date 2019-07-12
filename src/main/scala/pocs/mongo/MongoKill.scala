package pocs.mongo

/** Kills the MongoDB Daemon
  *
  * A simple app that checks if a mongod process is running and kills it.
  */
object MongoKill extends App {
  val kill: Process = Runtime.getRuntime.exec("""cmd /k "ps mongod | kill" """)
}
