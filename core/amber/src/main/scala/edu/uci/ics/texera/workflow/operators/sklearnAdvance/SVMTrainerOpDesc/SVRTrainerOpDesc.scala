package edu.uci.ics.texera.workflow.operators.sklearnAdvance.SVMTrainerOpDesc

import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.SklearnMLOperatorDescriptor

class SVRTrainerOpDesc extends SklearnMLOperatorDescriptor[SVRParameters] {
  override def getImportStatements(): String = {
    "from sklearn.svm import SVR"
  }

  override def getOperatorInfo(): String = {
    "SVM Regressor"
  }
}
