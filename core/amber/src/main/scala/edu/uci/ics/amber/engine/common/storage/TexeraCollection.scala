package edu.uci.ics.amber.engine.common.storage

abstract class TexeraCollection {
  def getDocuments(recursiveFlag: Boolean): List[TexeraDocument[_]]
  def getDocument(uri: TexeraURI): TexeraDocument[_]
  def createDocument(uri: TexeraURI): TexeraDocument[_]
  def deleteDocument(document: TexeraDocument[_]): Unit
}
