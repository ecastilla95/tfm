package exception

// This exception is thrown if the process cannot retrieve the key from the application.conf file
case class InvalidConfigurationException(message: String) extends Exception(message)
