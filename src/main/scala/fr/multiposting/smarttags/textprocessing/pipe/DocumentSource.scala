package fr.multiposting.smarttags.textprocessing.pipe

import fr.multiposting.smarttags.textprocessing.model.{SolrDocument, RawDocument, FileDocument}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.io.Source

import scala.concurrent.Future
import scala.util.Try

trait DocumentSource[T <: RawDocument] {
  def getMoreDocs(start: Long, rows: Long): Future[Seq[T]]
}

object SolrDocumentSource extends DocumentSource[SolrDocument] {

  private val solrUrl = "http://solr.multiposting.fr:8983/solr/opw_mpjobs_job"

  private val solrClient = SolrClient(solrUrl)

  private implicit val resultReads: Reads[SolrDocument] = (
    (__ \ 'key).read[String] and
      (__ \ 'description \ 'raw).read[String] and
      (__ \ 'description \ 'company).read[String] and
      (__ \ 'description \ 'position).read[String] and
      (__ \ 'description \ 'profile).read[String]
    )(SolrDocument.apply _)

  private val responseReads = (__ \ 'response \ 'docs).read[Seq[SolrDocument]]
  private val errorReads = (__ \ 'error \ 'msg).read[String]

  override def getMoreDocs(start: Long, rows: Long): Future[Seq[SolrDocument]] =
    solrClient
      .query("q" -> "*:*", "start" -> start.toString, "rows" -> rows.toString)
      .map { response =>
      response.asOpt(errorReads) foreach { msg => throw new RuntimeException(msg) }
      response.as(responseReads)
    }
}

case class FileDocumentSource(filePath: String) extends DocumentSource[FileDocument] {

  val source = Try(Source.fromFile(filePath)).map(_.getLines)

  override def getMoreDocs(start: Long, rows: Long): Future[Seq[FileDocument]] = {
    Future {
      source.map { it =>
        it.slice(start.toInt, (start + rows).toInt)
          .toList
          .zip(Stream from start.toInt)
          .map(u => FileDocument(u._2.toString, u._1))
      }.get
    }
  }

}