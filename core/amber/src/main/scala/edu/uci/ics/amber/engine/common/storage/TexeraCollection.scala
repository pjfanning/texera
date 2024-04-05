package edu.uci.ics.amber.engine.common.storage

abstract class TexeraCollection {
  def getDocuments(recursiveFlag: Boolean): List[TexeraDocument[_]]
  def createDocument(): TexeraDocument[_]
  def deleteDocument(document: TexeraDocument[_]): Unit
}
