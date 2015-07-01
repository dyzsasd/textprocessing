package fr.multiposting.smarttags.textprocessing.model

trait RawDocument {
  val key: String
}

case class SolrDocument(
  key: String,
  rawDesc: String,
  companyDesc: String,
  positionDesc: String,
  profileDesc: String
) extends RawDocument

case class FileDocument(
  key: String,
  line: String
) extends RawDocument