package pocs.lingpipe

import com.aliasi.classify.{Classification, Classified, DynamicLMClassifier, JointClassifier}
import com.aliasi.lm.NGramBoundaryLM
import com.aliasi.util.AbstractExternalizable

object LingPipeBasicExample extends App {

  val CATEGORIES = Array("catholic", "atheist")
  val NGRAM_SIZE = 6 // processes data in 6 character sequences

  //val text = "The Pope is a very important religious figure in the Catholic church" // sample text
  val catholic =
    """Speaking to the priests gathered for the Mass – which focuses especially on priestly unity – Pope Francis said,
      |“We [priests] must not forget that our evangelical models are those ‘people’, the ‘crowd’, with its real faces,
      |which the anointing of the Lord raises up and revives”. “They are an image of our soul, and an image of the Church”, he said.
      |“Each of them incarnates the one heart of our people.”
      |
      |The Pope “confessed” to the priests that when he anoints people, he is always generous with the oil:
      |“In that generous anointing, we can sense that our own anointing is being renewed”. Priests “are not distributors of bottled oil”, he said.
      |Rather, “We [priests] anoint by distributing ourselves, distributing our vocation and our heart. When we anoint other,
      |we ourselves are anointed anew by the faith and affection of our people”.
      |
      |Pope Francis concluded his homily with the prayer that the Father might “renew deep within us the spirit of holiness”,
      |that He might “grant that we may be one in imploring mercy for the people entrusted to our care, and for all the world”.""".stripMargin

  val noncatholic =
    """Religious education in schools needs a major overhaul to reflect an increasingly diverse world and should include the study of atheism,
      |agnosticism and secularism, a two-year investigation has concluded.
      |
      |The subject should be renamed Religion and Worldviews to equip young people with respect and empathy for different faiths and viewpoints,
      |says the Commission on Religious Education in a report published on Sunday.
      |
      |Content “must reflect the complex, diverse and plural nature of worldviews”, drawing from “a range of religious, philosophical, spiritual
      |and other approaches to life, including different traditions within Christianity, Buddhism, Hinduism, Islam, Judaism and Sikhism,
      |non-religious worldviews and concepts including humanism, secularism, atheism and agnosticism”.
      |
      |All pupils in publicly funded schools should study the subject up to year 11, the report says, but it falls short of recommending the abolition
      |of the right of parents to withdraw children from religious education. It comes three weeks after figures showed the number of pupils taking
      |religious studies at A-level this year had fallen by 22% compared with 2017, and two days after new data suggested more than half the population
      |has no religion.""".stripMargin

  // Creating the classifier
  val classifier: DynamicLMClassifier[NGramBoundaryLM] = DynamicLMClassifier.createNGramBoundary(CATEGORIES, NGRAM_SIZE)

  // Training the classifier
  def train(text: String, category: String): Unit = classifier.handle(new Classified[CharSequence](text, new Classification(category)))

  train(catholic, CATEGORIES(0))
  train(noncatholic, CATEGORIES(1))

  // Compiling the classifier
  val jointClassifier = AbstractExternalizable.compile(classifier).asInstanceOf[JointClassifier[CharSequence]]

  // Classifies the new text and shows statistics about it
  val newText =
    """President Trump phones Pope over Notre Dame fire
      |American President Donald Trump expresses his closeness to the Pope after the fire at the Paris Cathedral of Notre Dame
      |
      |Pope Francis received a phone call on Wednesday afternoon from the President of the United States, Donald Trump, who,
      |referring to the devastation of Notre Dame Cathedral, expressed his closeness to the Pope on behalf of the American people.
      |Alessandro Gisotti, interim director of the Holy See's Press Office, made the declaration in a Tweet on Wednesday evening.""".stripMargin

  val jointClassification = jointClassifier.classify(newText)
  val bestCategory = jointClassification.bestCategory()
  val details = jointClassification.toString

  println(bestCategory)
  println(details)

}