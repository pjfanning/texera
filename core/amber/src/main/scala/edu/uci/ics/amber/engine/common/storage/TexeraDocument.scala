package edu.uci.ics.amber.engine.common.storage

import java.io.{InputStream, OutputStream}

abstract class TexeraDocument[T >: Null <: AnyRef] {
  def readItem(i: Int): T = throw new UnsupportedOperationException("readItem method is not supported")

  def read(): Iterator[T] = throw new UnsupportedOperationException("read method is not supported")

  def readAsOutputStream(outputStream: OutputStream): Unit = throw new UnsupportedOperationException("readAsOutputStream method is not supported")

  def readAsInputStream(): InputStream = throw new UnsupportedOperationException("readAsInputStream method is not supported")

  def write(items: Iterator[T]): Unit = throw new UnsupportedOperationException("write method is not supported")

  def writeWithStream(inputStream: InputStream): Unit = throw new UnsupportedOperationException("writeWithStream method is not supported")

  def writeItem(item: T): Unit = throw new UnsupportedOperationException("writeItem method is not supported")

  def getURI: TexeraURI

  def delete(): Unit
}
