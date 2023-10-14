package edu.uci.ics.texera.workflow.operators.nn

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, TermQuery}
import org.apache.lucene.store.RAMDirectory

class TextKNN {
  private val directory = new RAMDirectory()
  private val analyzer = new StandardAnalyzer()
  private val K = 3

  def addDocument(text: String, sentiment: String): Unit = {
    val config = new IndexWriterConfig(analyzer)
    val writer = new IndexWriter(directory, config)
    val doc = new Document
    doc.add(new StringField("text", text, Field.Store.YES))
    doc.add(new StringField("sentiment", sentiment, Field.Store.YES))
    writer.addDocument(doc)
    writer.close()
  }

  def removeDocument(text: String): Unit = {
    val config = new IndexWriterConfig(analyzer)
    val writer = new IndexWriter(directory, config)
    writer.deleteDocuments(new Term("text", text))
    writer.close()
  }

  def predictSentiment(text: String): String = {
    addDocument(text, "")  // Temporarily add new text to the index

    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val hits = searcher.search(new TermQuery(new Term("text", text)), reader.maxDoc()).scoreDocs

    val sentimentCounts = hits.slice(1, K + 1).map(hit => {
      searcher.doc(hit.doc).get("sentiment")
    }).groupBy(identity).view.map(x => {x._1 -> x._2.length}).toMap

    reader.close()

    // Confidence check
    val goodCount = sentimentCounts.getOrElse("good", 0)
    val badCount = sentimentCounts.getOrElse("bad", 0)

    if (Math.abs(goodCount - badCount) > K / 2) {
      // Based on your threshold adjust the condition
      if (goodCount > badCount) "good" else "bad"
    } else {
      "neutral"
    }
  }
}

object TextKNNApp extends App {
  val knn = new TextKNN()
  knn.addDocument("I love this product", "good")
  knn.addDocument("This is the worst movie I've ever seen", "bad")

  println(knn.predictSentiment("This film is amazing and I adore it"))  // Likely 'good'
  println(knn.predictSentiment("I hate this so much"))  // Likely 'bad'
}

