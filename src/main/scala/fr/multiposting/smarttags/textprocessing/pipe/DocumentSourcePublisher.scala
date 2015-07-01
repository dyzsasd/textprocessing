package fr.multiposting.smarttags.textprocessing.pipe


import DocumentSourcePublisher._
import akka.actor.{Props, FSM}
import akka.actor.FSM.Event
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import fr.multiposting.smarttags.textprocessing.model.{RawDocument, SolrDocument}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


import scala.collection.immutable.Queue

object DocumentSourcePublisher {

  def props[T <: RawDocument]
    (start: Long, totalDocs: Long, documentSource: DocumentSource[T], cacheSize: Long = 100): Props =
    Props(new DocumentSourcePublisher[T](start, totalDocs, documentSource, cacheSize))

  sealed trait Data
  case class CurrentDocs[T <: RawDocument](
    currIndex: Long,
    buffer: Queue[T] = Queue.empty,
    delivered: Long = 0
  ) extends Data

  sealed trait State
  object Quering extends State
  object ready extends State

  sealed trait Message
  case class Documents[T <: RawDocument](docs: Seq[T])

}

class DocumentSourcePublisher[T <: RawDocument]
  (start: Long, totalDocs: Long, documentSource: DocumentSource[T], cacheSize: Long = 100)
  extends FSM[State, Data] with ActorPublisher[RawDocument] {

  startWith(Quering, CurrentDocs(start))

  override def preStart(): Unit = {
    documentSource
      .getMoreDocs(start, cacheSize)
      .map(Documents.apply)
      .foreach(self ! _)
  }

  when(Quering, 10.seconds) {
    case Event(StateTimeout, _) =>
      stop(FSM.Failure("Solr client query timeout"))

    case Event(docs: Documents[T], data: CurrentDocs[T]) =>
      val newCache = CurrentDocs(
        data.currIndex + cacheSize,
        docs.docs.foldLeft(data.buffer)((buffer, item) => buffer.enqueue(item)),
        data.delivered)
      self ! Request(0)
      goto(ready) using newCache
  }

  when(ready) {
    case Event(Request(n), data: CurrentDocs[T]) =>
      log.debug("Got request for {} items from downstream", n)
      nextState(data)
  }

  def nextState(data: CurrentDocs[T]): State = {
    if(data.delivered > totalDocs) {
      stop()
    } else if(data.buffer.nonEmpty && totalDemand > 0 && isActive) {
      val (message, tail) = data.buffer.dequeue
      nextState(data.copy(buffer = tail, delivered = data.delivered + 1))
    } else if (data.buffer.isEmpty && totalDemand > 0 && isActive) {
      SolrDocumentSource
        .getMoreDocs(data.currIndex, cacheSize)
        .map(Documents.apply)
        .foreach(self ! _)
      goto(Quering) using data
    } else if (data.buffer.nonEmpty && totalDemand <= 0 && isActive) {
      stay() using data
    } else {
      stop(FSM.Failure("cannot handle stats"))
    }
  }

}
