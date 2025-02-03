package edu.uci.ics.amber.engine.common

import akka.actor.{ActorSystem, Address, Cancellable, DeadLetter, Props}
import akka.serialization.{Serialization, SerializationExtension}
import com.typesafe.config.ConfigFactory.defaultApplication
import com.typesafe.config.{Config, ConfigFactory}
import edu.uci.ics.amber.clustering.ClusterListener
import edu.uci.ics.amber.engine.architecture.messaginglayer.DeadLetterMonitorActor

import java.io.{BufferedReader, InputStreamReader}
import java.net.{InetAddress, URL}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object AmberRuntime {

  var serde: Serialization = _
  private var _actorSystem: ActorSystem = _

  def actorSystem: ActorSystem = {
    _actorSystem
  }

  def scheduleCallThroughActorSystem(delay: FiniteDuration)(call: => Unit): Cancellable = {
    _actorSystem.scheduler.scheduleOnce(delay)(call)
  }

  def scheduleRecurringCallThroughActorSystem(initialDelay: FiniteDuration, delay: FiniteDuration)(
    call: => Unit
  ): Cancellable = {
    _actorSystem.scheduler.scheduleWithFixedDelay(initialDelay, delay)(() => call)
  }

  private def getNodeIpAddress: String = {
    try {
      val query = new URL("http://checkip.amazonaws.com")
      val in = new BufferedReader(new InputStreamReader(query.openStream()))
      val ip = in.readLine()
      val localIp = InetAddress.getLocalHost().getHostAddress()
      println(s"public++++++++++in.readLine(): $ip") // 打印 IP
      println(s"private——————————InetAddress.getLocalHost().getHostAddress(): $localIp") // 打印 IP
      //打印public++++++++++in.readLine(): 35.84.255.218
      //private——————————InetAddress.getLocalHost().getHostAddress(): 172.31.31.180
      ip
      //      InetAddress.getLocalHost().getHostAddress()
    } catch {
      case e: Exception => throw e
    }
  }

  def startActorMaster(clusterMode: Boolean): Unit = {
    var localIpAddress = "localhost"
    if (clusterMode) {
      localIpAddress = getNodeIpAddress

      val localPrivateIdAddress = InetAddress.getLocalHost().getHostAddress()

      val masterConfig = ConfigFactory
        .parseString(s"""
              akka.remote.artery.canonical.port = 2552
              akka.remote.artery.canonical.hostname = $localIpAddress
              akka.remote.artery.bind.hostname = $localPrivateIdAddress
              akka.remote.artery.bind.port = 2552
              akka.cluster.seed-nodes = [ "akka://Amber@$localIpAddress:2552" ]
              """)
        .withFallback(akkaConfig)
      AmberConfig.masterNodeAddr = createMasterAddress(localIpAddress)
      createAmberSystem(masterConfig)
    }
    else{
      val masterConfig = ConfigFactory
        .parseString(s"""
        akka.remote.artery.canonical.port = 2552
        akka.remote.artery.canonical.hostname = $localIpAddress
        akka.cluster.seed-nodes = [ "akka://Amber@$localIpAddress:2552" ]
        """)
        .withFallback(akkaConfig)
      AmberConfig.masterNodeAddr = createMasterAddress(localIpAddress)
      createAmberSystem(masterConfig)

    }
  }

  def akkaConfig: Config = ConfigFactory.load("cluster").withFallback(defaultApplication())

  private def createMasterAddress(addr: String): Address = Address("akka", "Amber", addr, 2552)

  def startActorWorker(mainNodeAddress: Option[String]): Unit = {
    val addr = mainNodeAddress.getOrElse("localhost")
    var localIpAddress = "localhost"
    if (mainNodeAddress.isDefined) {

      println(s"main++++++++++NodeAddress: $mainNodeAddress")
      //打印main++++++++++NodeAddress: Some(35.84.255.218)

      localIpAddress = getNodeIpAddress

      val localPrivateIdAddress = InetAddress.getLocalHost().getHostAddress()

      val workerConfig = ConfigFactory
        .parseString(s"""
              akka.remote.artery.canonical.hostname = $localIpAddress
              akka.remote.artery.canonical.port = 0
              akka.remote.artery.bind.hostname = $localPrivateIdAddress
              akka.remote.artery.bind.port = 0
              akka.cluster.seed-nodes = [ "akka://Amber@$addr:2552" ]
              """)
        .withFallback(akkaConfig)
      AmberConfig.masterNodeAddr = createMasterAddress(addr)
      createAmberSystem(workerConfig)
    }

    else{val workerConfig = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = $localIpAddress
        akka.remote.artery.canonical.port = 0
        akka.cluster.seed-nodes = [ "akka://Amber@$addr:2552" ]
        """)
      .withFallback(akkaConfig)
      AmberConfig.masterNodeAddr = createMasterAddress(addr)
      createAmberSystem(workerConfig)}
  }

  private def createAmberSystem(actorSystemConf: Config): Unit = {
    _actorSystem = ActorSystem("Amber", actorSystemConf)
    _actorSystem.actorOf(Props[ClusterListener](), "cluster-info")
    val deadLetterMonitorActor =
      _actorSystem.actorOf(Props[DeadLetterMonitorActor](), name = "dead-letter-monitor-actor")
    _actorSystem.eventStream.subscribe(deadLetterMonitorActor, classOf[DeadLetter])
    serde = SerializationExtension(_actorSystem)
  }
}
