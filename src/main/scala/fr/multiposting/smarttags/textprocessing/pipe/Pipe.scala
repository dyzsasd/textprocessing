package fr.multiposting.smarttags.textprocessing.pipe

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import fr.multiposting.smarttags.textprocessing.model.SolrDocument
import scala.concurrent.ExecutionContext.Implicits.global


object Pipe {
  implicit val system = ActorSystem("Text-processing")

  def startPipe: Unit = {
    implicit val materalizer = ActorFlowMaterializer()
    val solrActor = system.actorOf(DocumentSourcePublisher.props(0, 100, SolrDocumentSource, 20))

    val source = Source(ActorPublisher(solrActor))

    val sink = Sink.
  }

}
