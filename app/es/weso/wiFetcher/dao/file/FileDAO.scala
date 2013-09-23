package es.weso.wiFetcher.dao.file

import java.nio.charset.CodingErrorAction

import scala.io.Codec

import es.weso.wiFetcher.dao.DAO

trait FileDAO[T] extends DAO[T] {

  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  protected def load(path: String): List[T]

  protected def parseData(path: String): Seq[T]

}