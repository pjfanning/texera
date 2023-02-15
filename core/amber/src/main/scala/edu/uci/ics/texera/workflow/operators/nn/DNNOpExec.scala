package edu.uci.ics.texera.workflow.operators.nn

import org.deeplearning4j.nn.conf.GradientNormalization
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.LSTM
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Nadam
import org.nd4j.linalg.lossfunctions.LossFunctions
import edu.uci.ics.texera.workflow.common.operators.mlmodel.MLModelOpExec
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.api.ndarray.INDArray
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

class DNNOpExec(features: List[String], y:String, numLayers:Int) extends MLModelOpExec {

  var confBuilder: NeuralNetConfiguration.ListBuilder = new NeuralNetConfiguration.Builder()
    .seed(256)
    .weightInit(WeightInit.XAVIER)
    .updater(new Nadam)
    .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
    .gradientNormalizationThreshold(0.5) //Not always required, but helps with this data set
  .list().layer(new LSTM.Builder().activation(Activation.TANH).nIn(features.length).nOut(1).build)
    confBuilder = confBuilder.layer(new LSTM.Builder().activation(Activation.TANH).nIn(64).nOut(128).build)
    confBuilder = confBuilder.layer(new LSTM.Builder().activation(Activation.TANH).nIn(128).nOut(16).build)
  val conf: MultiLayerConfiguration = confBuilder.layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX).nIn(16).nOut(1).build).build

  val net = new MultiLayerNetwork(conf)
  net.init()

  override def getTotalEpochsCount: Int = 50

  override def predict(miniBatch: Array[Tuple]): Unit = {
  }

  override def calculateLossGradient(miniBatch: Array[Tuple]): Unit = {}

  override def readjustWeight(): Unit = {}

  override def outputPrediction(allData: Array[Tuple]): Array[Tuple] = allData
}
