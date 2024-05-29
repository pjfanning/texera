package edu.uci.ics.texera.workflow.operators.sklearnAdvance.SVMTrainerOpDesc

import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.SklearnMLOperatorDescriptor

class SVCTrainerOpDesc extends SklearnMLOperatorDescriptor[SVMParameters] {
  override def getImportStatements(): String = {
    "from sklearn.svm import SVC"
  }

  override def getOperatorInfo(): String = {
    "SVM Classifier"
  }
}
