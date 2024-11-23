package edu.uci.ics.texera.workflow.operators.sklearn

class SklearnLinearSVMOpDesc extends SklearnClassifierOpDesc {
  override def getImportStatements = "from sklearn.svm import LinearSVC"
  override def getUserFriendlyModelName = "Linear Support Vector Machine"
}
