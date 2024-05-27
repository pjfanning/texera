package edu.uci.ics.texera.workflow.operators.sklearnAdvance.KNNTrainerOpDesc

import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.SklearnMLOperatorDescriptor


class KNNRegressorTrainerOpDesc extends SklearnMLOperatorDescriptor[KNNParameters] {
  override def getImportStatements(): String = {
    "from sklearn.neighbors import KNeighborsRegressor"
  }

  override def getOperatorInfo(): String = {
    "KNN Regressor"
  }
}
