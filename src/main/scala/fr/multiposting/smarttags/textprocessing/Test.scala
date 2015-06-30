package fr.multiposting.smarttags.textprocessing

import fr.multiposting.smarttags.textprocessing.pipe.SolrConnection
import fr.multiposting.smarttags.textprocessing.pipe.Pipe.system

object Test extends App {
  import system.dispatcher
  SolrConnection.getMoreDocs(10, 5)
}
