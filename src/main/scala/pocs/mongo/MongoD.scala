package pocs.mongo

/** Starts the MongoDB Daemon
  *
  * A simple app that checks if a mongod process is running and kills it.
  * Then, it starts a new one.
  */
object MongoD extends App {
  val reset: Process = Runtime.getRuntime.exec("""cmd /k "ps mongod | kill" """)
  val mongod: Process = Runtime.getRuntime.exec("""cmd /k "mongod --dbpath D:\\data" """)
}
