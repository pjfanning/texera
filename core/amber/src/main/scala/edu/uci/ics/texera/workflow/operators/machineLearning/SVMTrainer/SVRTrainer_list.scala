package edu.uci.ics.texera.workflow.operators.machineLearning.SVMTrainer

import edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass.SklearnMLOperatorDescriptor

class SVRTrainer_list extends SklearnMLOperatorDescriptor[SVCParameters]{

  override def getImportStatements(): String = {
    "from sklearn.svm import SVR"
  }

  override def getOperatorInfo(): String = {
    "SVR"
  }
}
