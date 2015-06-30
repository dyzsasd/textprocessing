package fr.multiposting.smarttags.textprocessing.pipe

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import fr.multiposting.smarttags.textprocessing.model.RawDocument
import spray.client.pipelining._
import spray.http.FormData
import spray.http.HttpEntity
import spray.http.HttpRequest
import spray.http.MediaTypes._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import scala.concurrent.Future

import scala.concurrent.ExecutionContext
import collection.JavaConversions._

import scala.util.{Success, Failure}

import fr.multiposting.smarttags.textprocessing.pipe.Pipe.system

object SolrConnection {

  import system.dispatcher

  val solrUrl = "http://solr.multiposting.fr:8983/solr/opw_mpjobs_job/select?"

  private val pipeline: HttpRequest => Future[Seq[RawDocument]] =
    sendReceive ~> unmarshal[Seq[RawDocument]]

  private implicit val resultReads: Reads[RawDocument] =(
      (__ \ 'key).read[String] and
      (__ \ 'description__raw).read[String] and
      (__ \ 'description__company).read[String] and
      (__ \ 'description__position).read[String] and
      (__ \ 'description__profile).read[String]
    )(RawDocument.apply _)

  private val responseReads = (__ \ 'response \ 'docs).read[Seq[RawDocument]]
  private val errorReads = (__ \ 'error \ 'msg).read[String]

  private def requestBuilder(baseUrl: String)(start: Long, rows: Long) = {
    val req = baseUrl + Seq(
      "q" -> "*:*",
      "start" -> start.toString,
      "rows" -> rows.toString,
      "sort" -> "key+asc",
      "wt" -> "json"
    ).map(t => t._1 + "=" + t._2).mkString("&")
    println(req)
    req
  }

  implicit val unmarshaller = Unmarshaller[Seq[RawDocument]](`application/json`) {
    case HttpEntity.NonEmpty(contentType, data) =>
      println("ite is ok" + contentType.toString())
      println(data)
      Seq[RawDocument]()
      //val response = Json.parse(data.toByteArray)
      //response.asOpt(errorReads) foreach { msg => throw new RuntimeException(msg) }
      //response.as(responseReads)
    case _ =>
      println("problem error")
      Seq[RawDocument]()
  }

  def getMoreDocs(start: Long, rows: Long)(implicit ec: ExecutionContext): Unit ={
    val get = Get(requestBuilder(solrUrl)(start, rows))
    println(get)
    pipeline(get).onComplete {
      case Success(posts) => println(posts)
      case Failure(t) =>
        t.printStackTrace()
    }

  }

}
