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

  final val PREPROCESSING_GROUP = "Preprocessing"
  final val MODEL_TRAINING_GROUP = "Model Training"
  final val MODEL_PERFORMANCE_GROUP = "Model Performance"
  final val MODEL_VISUALIZATION_GROUP = "Model Visualization"

  /**
    * The order of the groups to show up in the frontend operator panel.
    * The order numbers are relative.
    */
  final val OperatorGroupOrderList: List[GroupInfo] = List(
    GroupInfo(INPUT_GROUP, 0, null),
    GroupInfo(DATABASE_GROUP, 1, null),
    GroupInfo(SEARCH_GROUP, 2, null),
    GroupInfo(CLEANING_GROUP, 3, null),
    GroupInfo(MACHINE_LEARNING_GROUP, 4, List(
      GroupInfo(PREPROCESSING_GROUP, 101, null),
      GroupInfo(MODEL_TRAINING_GROUP, 102, null),
      GroupInfo(MODEL_PERFORMANCE_GROUP, 103, null),
      GroupInfo(MODEL_VISUALIZATION_GROUP, 104, null),
    )),
    GroupInfo(JOIN_GROUP, 5, null),
    GroupInfo(SET_GROUP, 6, null),
    GroupInfo(AGGREGATE_GROUP, 7, null),
    GroupInfo(SORT_GROUP, 8, null),
    GroupInfo(UTILITY_GROUP, 9, null),
    GroupInfo(API_GROUP, 10, null),
    GroupInfo(UDF_GROUP, 11, null),
    GroupInfo(VISUALIZATION_GROUP, 12, null)
  )
}


