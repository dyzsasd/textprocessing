package fr.multiposting.smarttags.textprocessing

import fr.multiposting.smarttags.textprocessing.pipe.SolrDocumentSource
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Test extends App {
  val raw = SolrDocumentSource.getMoreDocs(21, 56).map(println)
  while(!raw.isCompleted) {}
}
