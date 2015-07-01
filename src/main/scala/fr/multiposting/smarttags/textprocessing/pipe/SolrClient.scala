package fr.multiposting.smarttags.textprocessing.pipe

import java.io.{Reader, InputStream}

import akka.actor.{ActorRef, FSM}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import fr.multiposting.smarttags.textprocessing.model.SolrDocument
import org.apache.commons.io.IOUtils
import org.apache.solr.client.solrj.{SolrQuery, SolrRequest, ResponseParser}
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.common.SolrException
import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.solr.common.util.NamedList
import play.api.libs.json._
import play.api.libs.functional.syntax._


import scala.concurrent._
import scala.util.control.NonFatal

case class SolrClient(solrUrl: String) {

  private val server = new HttpSolrClient(solrUrl)

  private val Response = "response"

  def query(params: (String, String)*): Future[JsObject] = {
    val query = new SolrQuery()
    params.foreach { t =>
      query.set(t._1, t._2)
    }
    val request = new QueryRequest(query, SolrRequest.METHOD.POST)
    request.setResponseParser(new JSONResponseParser)
    println(request)
    Future { blocking(server.request(request)) }
      .map(_.get(Response).asInstanceOf[JsObject])
  }

  private class JSONResponseParser extends ResponseParser {

    def getWriterType: String = "default"  // Do not use "json"

    def processResponse(body: InputStream, encoding: String): NamedList[AnyRef] = {
      val input = IOUtils.toByteArray(body)
      println(input.length)
      val response = new NamedList[AnyRef]()
      try {
        response.add(Response, Json.parse(input).as[JsObject])
      } catch {
        case NonFatal(e) =>
          throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Parsing error", e)
      }
      response
    }

    def processResponse(reader: Reader): NamedList[AnyRef] =
      throw new RuntimeException("Cannot handle character stream")

    override def getContentType: String = "application/json; charset=UTF-8"

    override def getVersion: String = "1"

  }

}