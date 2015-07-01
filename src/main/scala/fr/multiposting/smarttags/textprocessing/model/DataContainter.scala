package fr.multiposting.smarttags.textprocessing.model

trait PipeContainer {
  val key: String
}

case class TextContainer(key: String, text: String) extends PipeContainer

case class WordContainer(key: String, words: Seq[String]) extends PipeContainer


