package edu.uci.ics.texera.workflow.operators.sklearnAdvance.KNNTrainerOpDesc
import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.SklearnMLOperatorDescriptor


class KNNClassifierTrainerOpDesc extends SklearnMLOperatorDescriptor[KNNParameters] {
  override def getImportStatements(): String = {
    "from sklearn.neighbors import KNeighborsClassifier"
  }

  override def getOperatorInfo(): String = {
    "KNN Classifier"
  }
}
