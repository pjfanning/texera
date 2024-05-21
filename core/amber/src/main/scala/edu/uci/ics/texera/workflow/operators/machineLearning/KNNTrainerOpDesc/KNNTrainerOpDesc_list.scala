package edu.uci.ics.texera.workflow.operators.machineLearning.KNNTrainerOpDesc

import com.fasterxml.jackson.annotation.JsonProperty
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.operators.machineLearning.AbstractClass.SklearnMLOperatorDescriptor

class KNNTrainerOpDesc_list extends SklearnMLOperatorDescriptor[KNNParameters]{
  override def getImportStatements(): String = {
    "from sklearn.neighbors import KNeighborsClassifier"
  }

  override def getOperatorInfo(): String = {
    "KNN"
  }
}
