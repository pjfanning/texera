package edu.uci.ics.texera.workflow.operators.sklearn

class SklearnBaggingOpDesc extends SklearnClassifierOpDesc {
  override def getImportStatements = "from sklearn.ensemble import BaggingClassifier"
  override def getUserFriendlyModelName = "Bagging"
}
