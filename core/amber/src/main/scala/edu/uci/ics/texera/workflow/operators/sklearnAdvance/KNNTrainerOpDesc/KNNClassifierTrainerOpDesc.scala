
import com.fasterxml.jackson.annotation.JsonProperty
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.operators.sklearnAdvance.AbstractClass.SklearnMLOperatorDescriptor
import edu.uci.ics.texera.workflow.operators.sklearnAdvance.KNNTrainerOpDesc.KNNParameters


class KNNClassifierTrainerOpDesc extends SklearnMLOperatorDescriptor[KNNParameters] {
  override def getImportStatements(): String = {
    "from sklearn.neighbors import KNeighborsClassifier"
  }

  override def getOperatorInfo(): String = {
    "KNN Classifier"
  }
}
