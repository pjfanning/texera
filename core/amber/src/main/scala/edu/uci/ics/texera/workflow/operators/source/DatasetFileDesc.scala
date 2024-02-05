package edu.uci.ics.texera.workflow.operators.source

class DatasetFileDesc(val filePath: String, val datasetPath: String, val versionHash: String) {
  override def toString: String = s"DatasetFileDesc(filePath=$filePath, datasetPath=$datasetPath, versionHash=$versionHash)"
}