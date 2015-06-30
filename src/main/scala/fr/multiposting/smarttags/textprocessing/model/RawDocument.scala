package fr.multiposting.smarttags.textprocessing.model

case class RawDocument(
  key: String,
  rawDesc: String,
  companyDesc: String,
  positionDesc: String,
  profileDesc: String
)