package edu.uci.ics.amber.engine.common.storage.file

import java.nio.file.Path

trait FSTreeNode {
  def getAbsolutePath: Path

  def getRelativePath: Path

  def getChildren: List[FSTreeNode]
}
