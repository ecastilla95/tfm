package pocs.lingpipe

case class EvaluationResults(numberOfTests: Int, numberOfSuccesses: Int){
  def getRatio: Double = numberOfSuccesses.toDouble / numberOfTests
}