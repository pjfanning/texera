package edu.uci.ics.texera.workflow.operators.sklearn

class SklearnLogisticRegressionCVOpDesc extends SklearnClassifierOpDesc {
  override def getImportStatements = "from sklearn.linear_model import LogisticRegressionCV"
  override def getUserFriendlyModelName = "Logistic Regression Cross Validation"
}
