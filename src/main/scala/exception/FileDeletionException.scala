package exception

case class FileDeletionException(path: String) extends Exception(s"Unable to delete $path")
