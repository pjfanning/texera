package edu.uci.ics.texera.workflow.operators.machineLearning.SVCTrainer

import edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass.SklearnMLOperatorDescriptor

class SVCTrainer_list extends SklearnMLOperatorDescriptor[SVCParameters]{

  override def getImportStatements(): String = {
    "from sklearn.svm import SVC"
  }

  override def getOperatorInfo(): String = {
    "SVC"
  }
}
