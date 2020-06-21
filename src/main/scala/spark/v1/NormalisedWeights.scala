package spark.v1

// Date and weight of the sentiment analysis result on news of a given day and origin
case class NormalisedWeights(origin: String, date: String, weight: Double)
