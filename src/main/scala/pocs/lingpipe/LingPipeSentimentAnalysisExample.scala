package pocs.lingpipe

import java.io.File

import com.aliasi.classify.{Classification, Classified, DynamicLMClassifier}
import com.aliasi.lm.NGramProcessLM
import com.aliasi.util.Files
import commons.ProcessConstants

object LingPipeSentimentAnalysisExample extends App {
  try {
    val pb = new PolarityBasic
    pb.run()
  } catch {
    case t: Throwable => println("Thrown: " + t)
      t.printStackTrace()
  }
}

class PolarityBasic {
  val mPolarityDir: File = new File(ProcessConstants.DATA_FOLDER + "lingpipe/review_polarity", "txt_sentoken")
  val mCategories: Array[String] = mPolarityDir.list
  val nGram: Int = 8
  val encoding = "ISO-8859-1"

  val mClassifier: DynamicLMClassifier[NGramProcessLM] = DynamicLMClassifier.createNGramProcess(mCategories, nGram)

  def run(): Unit = {
    train()
    val results = evaluate()
    println("Number of test cases = " + results.numberOfTests)
    println("Number of correct cases = " + results.numberOfSuccesses)
    println(" % Correct = " + results.getRatio)
  }


  def train(): Unit = {
    for { category <- mCategories
          classification = new Classification(category)
          trainFile <- new File(mPolarityDir, category).listFiles().filter(isTrainingFile)
    }{
      val review = Files.readFromFile(trainFile, encoding)
      val classified = new Classified[CharSequence](review, classification)
      mClassifier.handle(classified)
    }

  }

  // We take the first 900 files for training and the remaining 100 ones for evaluation
  def isTrainingFile(file: File): Boolean = file.getName.charAt(2) != '9'

  def evaluate(): EvaluationResults = {
    val testResults = for {
      category <- mCategories
      testFile <- new File(mPolarityDir, category).listFiles().filterNot(isTrainingFile)
    } yield {
      val review = Files.readFromFile(testFile, encoding)
      if(mClassifier.classify(review).bestCategory() == category) 1 else 0
    }
    EvaluationResults(testResults.length, testResults.sum)
  }
}
