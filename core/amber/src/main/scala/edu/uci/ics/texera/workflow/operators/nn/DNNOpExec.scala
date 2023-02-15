package edu.uci.ics.texera.workflow.operators.nn

import ai.djl.Application
import ai.djl.nn.{Blocks, SequentialBlock}
import ai.djl.nn.Activation
import ai.djl.nn.core.Linear
import ai.djl.Model
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.Shape
import ai.djl.training.dataset.RandomAccessDataset.BaseBuilder
import ai.djl.training.dataset.{Dataset, RandomAccessDataset, Record}
import ai.djl.training.{DefaultTrainingConfig, EasyTrain, Trainer}
import ai.djl.training.evaluator.Accuracy
import ai.djl.ndarray.NDList
import ai.djl.training.loss.Loss
import ai.djl.util.Progress
import edu.uci.ics.texera.workflow.common.operators.mlmodel.MLModelOpExec
import edu.uci.ics.texera.workflow.common.tuple.Tuple

class DNNOpExec(features: List[String], y:String, numLayers:Int) extends MLModelOpExec {

  val application:Application = Application.Tabular.LINEAR_REGRESSION

  val block = new SequentialBlock()
  block.add(Blocks.batchFlattenBlock(features.length))
  block.add(Linear.builder.setUnits(128).build)
  block.add(Activation.reluBlock())
  block.add(Linear.builder.setUnits(64).build)
  block.add(Activation.reluBlock())
  block.add(Linear.builder.setUnits(1).build)

  val model: Model = Model.newInstance("mlp")
  model.setBlock(block)

  val config: DefaultTrainingConfig = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss)
    .addEvaluator(new Accuracy()) //softmaxCrossEntropyLoss is a standard loss for classification problems

  // Now that we have our training configuration, we should create a new trainer for our model
  val trainer: Trainer = model.newTrainer(config)

  trainer.initialize(new Shape(1, features.length))

  class TrainingDatasetBuilder extends BaseBuilder[TrainingDatasetBuilder]{
    override def self(): TrainingDatasetBuilder = this
  }

  class TrainingDataset(builder: BaseBuilder[_], miniBatch: Array[Tuple]) extends RandomAccessDataset(builder){
    override def get(manager: NDManager, index: Long): Record = {
      val tuple = miniBatch(index.toInt)
      val datum = manager.create(features.map(x => tuple.getField(x)).toArray[Double])
      val label = manager.create(tuple.getField(y).asInstanceOf[Double])
      new Record(new NDList(datum), new NDList(label))
    }

    override def availableSize(): Long = {
      miniBatch.length
    }

    override def prepare(progress: Progress): Unit = {}
  }

  override def getTotalEpochsCount: Int = 50

  override def predict(miniBatch: Array[Tuple]): Unit = {
    val dataset = new TrainingDataset(new TrainingDatasetBuilder(), miniBatch)
    EasyTrain.fit(trainer, 1, dataset, null)
  }

  override def calculateLossGradient(miniBatch: Array[Tuple]): Unit = {}

  override def readjustWeight(): Unit = {}

  override def outputPrediction(allData: Array[Tuple]): Array[Tuple] = allData
}
