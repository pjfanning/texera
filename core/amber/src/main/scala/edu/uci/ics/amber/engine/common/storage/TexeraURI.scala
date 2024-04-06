package edu.uci.ics.amber.engine.common.storage
import java.net.{URI, URISyntaxException}

object TexeraURI {

  val FILE_SCHEMA = "file"

  private val supportedSchemas = Set(FILE_SCHEMA)

  def apply(schema: String, uriBody: String): TexeraURI = {
    if (!supportedSchemas.contains(schema)) {
      throw new RuntimeException("Given schema is not supported")
    }
    new TexeraURI(new URI(schema + "://" + uriBody))
  }
}

class TexeraURI private (val uri: URI) {
  // expose some URI operations
  def getScheme: String = uri.getScheme
  def getPath: String = uri.getPath
  def getHost: String = uri.getHost
  def getURI: URI = uri
  def containsChildPath(other: TexeraURI): Boolean = {
    getScheme == other.getScheme && other.getPath != getPath && other.getPath.startsWith(getPath)
  }
  override def toString: String = uri.toString

  override def equals(other: Any): Boolean = other match {
    case that: TexeraURI => this.uri == that.uri
    case _ => false
  }

  override def hashCode(): Int = uri.hashCode()
}
