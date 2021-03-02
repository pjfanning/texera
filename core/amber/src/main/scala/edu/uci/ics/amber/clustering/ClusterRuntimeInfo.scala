package edu.uci.ics.amber.clustering

import akka.actor.ActorRef

import scala.collection.mutable

object ClusterRuntimeInfo {
  val controllers: mutable.HashSet[ActorRef] = mutable.HashSet[ActorRef]()
}
