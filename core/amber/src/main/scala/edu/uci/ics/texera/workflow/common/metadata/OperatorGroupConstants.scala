package edu.uci.ics.texera.workflow.common.metadata

object OperatorGroupConstants {
  final val INPUT_GROUP = "Data Input"
  final val DATABASE_GROUP = "Database Connector"
  final val SEARCH_GROUP = "Search"
  final val CLEANING_GROUP = "Data Cleaning"
  final val MACHINE_LEARNING_GROUP = "Machine Learning"
  final val JOIN_GROUP = "Join"
  final val SET_GROUP = "Set"
  final val AGGREGATE_GROUP = "Aggregate"
  final val SORT_GROUP = "Sort"
  final val UTILITY_GROUP = "Utilities"
  final val API_GROUP = "External API"
  final val UDF_GROUP = "User-defined Functions"
  final val VISUALIZATION_GROUP = "Visualization"
  final val HUGGINGFACE_GROUP = "Hugging Face"
  final val SKLEARN_GROUP = "Sklearn"

  final val PREPROCESSING_GROUP = "Preprocessing"
  final val MODEL_TRAINING_GROUP = "Model Training"
  final val MODEL_PERFORMANCE_GROUP = "Model Performance"
  final val MODEL_VISUALIZATION_GROUP = "Model Visualization"

  /**
    * The order of the groups to show up in the frontend operator panel.
    * The order numbers are relative.
    */
  final val OperatorGroupOrderList: List[GroupInfo] = List(
    GroupInfo(INPUT_GROUP),
    GroupInfo(DATABASE_GROUP),
    GroupInfo(SEARCH_GROUP),
    GroupInfo(CLEANING_GROUP),
//    GroupInfo(MACHINE_LEARNING_GROUP, List(GroupInfo(SKLEARN_GROUP), GroupInfo(HUGGINGFACE_GROUP),
//      GroupInfo(PREPROCESSING_GROUP),
//      GroupInfo(MODEL_TRAINING_GROUP),
//      GroupInfo(MODEL_PERFORMANCE_GROUP),
//      GroupInfo(MODEL_VISUALIZATION_GROUP),
//    )),
    GroupInfo(MACHINE_LEARNING_GROUP, List(
      GroupInfo(PREPROCESSING_GROUP),
      GroupInfo(MODEL_TRAINING_GROUP),
      GroupInfo(MODEL_PERFORMANCE_GROUP),
      GroupInfo(MODEL_VISUALIZATION_GROUP),
    )),
    GroupInfo(JOIN_GROUP),
    GroupInfo(SET_GROUP),
    GroupInfo(AGGREGATE_GROUP),
    GroupInfo(SORT_GROUP),
    GroupInfo(UTILITY_GROUP),
    GroupInfo(API_GROUP),
    GroupInfo(UDF_GROUP),
    GroupInfo(VISUALIZATION_GROUP)
  )
}


