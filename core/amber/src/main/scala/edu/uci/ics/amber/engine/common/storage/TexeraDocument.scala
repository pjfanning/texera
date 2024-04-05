package edu.uci.ics.amber.engine.common.storage

abstract class TexeraDocument[T >: Null <: AnyRef] {
  def readItem(i: Int): T;
  def read(): Iterator[T];
  def write(items: Iterator[T]): Unit;
  def writeItem(item: T): Unit;
}
