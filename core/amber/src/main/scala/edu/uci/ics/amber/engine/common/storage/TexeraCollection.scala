package edu.uci.ics.amber.engine.common.storage

abstract class TexeraCollection {
  def getURI: TexeraURI
  def getDocuments: List[TexeraDocument[_]]
  def createDocument(name: String): TexeraDocument[_]
  def rm(): Unit
}
