package fr.multiposting.smarttags.textprocessing.pipe

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import fr.multiposting.smarttags.textprocessing.model.RawDocument

object Pipe {
  implicit val system = ActorSystem("Text-processing")

  def startPipe: Unit = {
    implicit val materalizer = ActorFlowMaterializer()

  }
}
