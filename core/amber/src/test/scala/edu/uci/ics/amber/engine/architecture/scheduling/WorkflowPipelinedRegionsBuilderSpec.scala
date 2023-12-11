package edu.uci.ics.amber.engine.architecture.scheduling

import edu.uci.ics.amber.engine.e2e.TestOperators
import edu.uci.ics.amber.engine.e2e.TestUtils.buildWorkflow
import edu.uci.ics.texera.workflow.common.workflow.{LogicalLink, OperatorPort}
import edu.uci.ics.texera.workflow.operators.split.SplitOpDesc
import edu.uci.ics.texera.workflow.operators.udf.python.{
  DualInputPortsPythonUDFOpDescV2,
  PythonUDFOpDescV2
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class WorkflowPipelinedRegionsBuilderSpec extends AnyFlatSpec with MockFactory {

  "Pipelined Regions" should "correctly find regions in headerlessCsv->keyword->sink workflow" in {
    val headerlessCsvOpDesc = TestOperators.headerlessSmallCsvScanOpDesc()
    val keywordOpDesc = TestOperators.keywordSearchOpDesc("column-1", "Asia")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(headerlessCsvOpDesc, keywordOpDesc, sink),
      List(
        LogicalLink(
          OperatorPort(headerlessCsvOpDesc.operatorIdentifier, 0),
          OperatorPort(keywordOpDesc.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(keywordOpDesc.operatorIdentifier, 0),
          OperatorPort(sink.operatorIdentifier, 0)
        )
      )
    )

    val pipelinedRegions = workflow.executionPlan.regionsToSchedule
    assert(pipelinedRegions.size == 1)
  }

  "Pipelined Regions" should "correctly find regions in csv->(csv->)->join->sink workflow" in {
    val headerlessCsvOpDesc1 = TestOperators.headerlessSmallCsvScanOpDesc()
    val headerlessCsvOpDesc2 = TestOperators.headerlessSmallCsvScanOpDesc()
    val joinOpDesc = TestOperators.joinOpDesc("column-1", "column-1")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(
        headerlessCsvOpDesc1,
        headerlessCsvOpDesc2,
        joinOpDesc,
        sink
      ),
      List(
        LogicalLink(
          OperatorPort(headerlessCsvOpDesc1.operatorIdentifier, 0),
          OperatorPort(joinOpDesc.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(headerlessCsvOpDesc2.operatorIdentifier, 0),
          OperatorPort(joinOpDesc.operatorIdentifier, 1)
        ),
        LogicalLink(
          OperatorPort(joinOpDesc.operatorIdentifier, 0),
          OperatorPort(sink.operatorIdentifier, 0)
        )
      )
    )

    val pipelinedRegions = workflow.executionPlan.regionsToSchedule
    val ancestorMapping = workflow.executionPlan.regionAncestorMapping
    assert(pipelinedRegions.size == 2)

    val buildRegion = pipelinedRegions
      .find(v => v.operators.toList.exists(op => op.operator == headerlessCsvOpDesc1.operatorId))
      .get
    val probeRegion = pipelinedRegions
      .find(v => v.operators.toList.exists(op => op.operator == headerlessCsvOpDesc2.operatorId))
      .get

    assert(ancestorMapping(probeRegion).size == 1)
    assert(ancestorMapping(probeRegion).contains(buildRegion))
    assert(buildRegion.blockingDownstreamOperatorsInOtherRegions.length == 1)
    assert(
      buildRegion.blockingDownstreamOperatorsInOtherRegions.exists(pair =>
        pair._1.operator == joinOpDesc.operatorId
      )
    )
  }

  "Pipelined Regions" should "correctly find regions in csv->->filter->join->sink workflow" in {
    val headerlessCsvOpDesc1 = TestOperators.headerlessSmallCsvScanOpDesc()
    val keywordOpDesc = TestOperators.keywordSearchOpDesc("column-1", "Asia")
    val joinOpDesc = TestOperators.joinOpDesc("column-1", "column-1")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(
        headerlessCsvOpDesc1,
        keywordOpDesc,
        joinOpDesc,
        sink
      ),
      List(
        LogicalLink(
          OperatorPort(headerlessCsvOpDesc1.operatorIdentifier, 0),
          OperatorPort(joinOpDesc.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(headerlessCsvOpDesc1.operatorIdentifier, 0),
          OperatorPort(keywordOpDesc.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(keywordOpDesc.operatorIdentifier, 0),
          OperatorPort(joinOpDesc.operatorIdentifier, 1)
        ),
        LogicalLink(
          OperatorPort(joinOpDesc.operatorIdentifier, 0),
          OperatorPort(sink.operatorIdentifier, 0)
        )
      )
    )
    val pipelinedRegions = workflow.executionPlan.regionsToSchedule
    assert(pipelinedRegions.size == 2)
  }

  "Pipelined Regions" should "correctly find regions in buildcsv->probecsv->hashjoin->hashjoin->sink workflow" in {
    val buildCsv = TestOperators.headerlessSmallCsvScanOpDesc()
    val probeCsv = TestOperators.smallCsvScanOpDesc()
    val hashJoin1 = TestOperators.joinOpDesc("column-1", "Region")
    val hashJoin2 = TestOperators.joinOpDesc("column-2", "Country")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(
        buildCsv,
        probeCsv,
        hashJoin1,
        hashJoin2,
        sink
      ),
      List(
        LogicalLink(
          OperatorPort(buildCsv.operatorIdentifier, 0),
          OperatorPort(hashJoin1.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(probeCsv.operatorIdentifier, 0),
          OperatorPort(hashJoin1.operatorIdentifier, 1)
        ),
        LogicalLink(
          OperatorPort(buildCsv.operatorIdentifier, 0),
          OperatorPort(hashJoin2.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(hashJoin1.operatorIdentifier, 0),
          OperatorPort(hashJoin2.operatorIdentifier, 1)
        ),
        LogicalLink(
          OperatorPort(hashJoin2.operatorIdentifier, 0),
          OperatorPort(sink.operatorIdentifier, 0)
        )
      )
    )
    val pipelinedRegions = workflow.executionPlan.regionsToSchedule
    assert(pipelinedRegions.size == 2)
  }

  "Pipelined Regions" should "correctly find regions in csv->split->training-infer workflow" in {
    val csv = TestOperators.headerlessSmallCsvScanOpDesc()
    val split = new SplitOpDesc()
    val training = new PythonUDFOpDescV2()
    val inference = new DualInputPortsPythonUDFOpDescV2()
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(
        csv,
        split,
        training,
        inference,
        sink
      ),
      List(
        LogicalLink(
          OperatorPort(csv.operatorIdentifier, 0),
          OperatorPort(split.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(split.operatorIdentifier, 0),
          OperatorPort(training.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(training.operatorIdentifier, 0),
          OperatorPort(inference.operatorIdentifier, 0)
        ),
        LogicalLink(
          OperatorPort(split.operatorIdentifier, 1),
          OperatorPort(inference.operatorIdentifier, 1)
        ),
        LogicalLink(
          OperatorPort(inference.operatorIdentifier, 0),
          OperatorPort(sink.operatorIdentifier, 0)
        )
      )
    )
    val pipelinedRegions = workflow.executionPlan.regionsToSchedule
    assert(pipelinedRegions.size == 2)
  }

}
