package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.architecture.common.{LogicalExecutionSnapshot, ProcessingHistory}
import edu.uci.ics.texera.web.service.{ReplayCheckpointPlanner, WorkflowReplayManager}

import java.io.{FileInputStream, ObjectInputStream}
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.util.Random

object JsonTest {

  def main(args: Array[String]): Unit = {


    val file = Paths.get("").resolve("latest-interation-history")
    if(!Files.exists(file)){
      println("no interaction history found!")
      return
    }
    val ois = new ObjectInputStream(new FileInputStream(file.toFile))
    var history = ois.readObject.asInstanceOf[ProcessingHistory]
    ois.close()

    history.historyArray.foreach{
      i =>
        var cost = 0L
        try{
          cost = history.getSnapshot(i).checkpointCost
        }catch{
          case x: Throwable =>
        }
        println(i+" "+cost)
    }
//    println("-----------------------------------------------")
//    history.historyArray.foreach {
//      i =>
//        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF1-SortPartitions-operator-4e18bee2-0a12-4478-9482-9bf1a7d32efb-main-0"))
//        println(cost)
//    }
//    println("-----------------------------------------------")
//    history.historyArray.foreach {
//      i =>
//        val cost = history.getSnapshot(i).getCheckpointCost(ActorVirtualIdentity("Worker:WF1-Aggregate-operator-e8482e2e-0024-4fe6-8030-3e4db45abe02-localAgg-0"))
//        println(cost+" "+(cost < costThreshold))
//    }
//    history = new ProcessingHistory()
//    val time = Array(3000, 6000, 9000, 12000, 15000, 18000, 21000, 24000, 27000, 30000, 33000, 36000, 39000, 42000, 45000, 48000, 51000, 54000, 57000, 60000, 63000, 66000, 69000, 72000, 75000, 78000, 81000, 84000, 87000, 90000, 93000, 96000, 99000, 102000, 105000, 108000, 111000, 114000, 117000, 120000, 123000, 126000, 129000, 132000, 135000, 138000, 141000, 144000, 147000, 150000, 153000, 156000, 159000, 162000, 165000, 168000, 171000, 174000, 177000, 180000, 183000, 186000, 189000, 192000, 195000, 198000, 201000, 204000, 207000, 210000, 213000, 216000, 219000, 222000, 225000, 228000, 231000, 234000, 237000, 240000, 243000, 246000, 249000, 252000, 255000, 258000, 261000, 264000, 267000, 270000, 273000, 276000, 279000, 282000, 285000, 288000, 291000, 294000, 297000, 300000, 303000, 306000, 309000, 312000, 315000, 318000, 321000, 324000, 327000, 330000, 333000, 336000, 339000, 342000, 345000, 348000, 351000, 354000, 357000, 360000, 363000, 366000, 369000, 372000, 375000, 378000, 381000, 384000, 387000, 390000, 393000, 396000)
//    val cost = Array(16706655, 33413310, 50119965, 66826620, 83533275, 100239930, 116946585, 133653240, 150359895, 167066550, 179909465, 192722528, 205535592, 218348655, 231161719, 243974783, 256787846, 269600910, 282413973, 295227037, 306439188, 317638971, 328838754, 340038537, 351238320, 362438103, 373637886, 384837669, 396037452, 407237235, 418855401, 430476800, 442098198, 453719596, 465340995, 476962393, 488583791, 500205190, 511826588, 523447986, 519266603, 514963130, 510659656, 506356182, 502052709, 497749235, 493445762, 489142288, 484838814, 480535341, 480485656, 480468836, 480452016, 480435196, 480418376, 480401556, 480384735, 480367915, 480351095, 480334275, 480348004, 480361979, 480375954, 480389929, 480403905, 480417880, 480431855, 480445830, 480459805, 480473781, 480466723, 480459495, 480452268, 480445040, 480437812, 480430585, 480423357, 480416130, 480408902, 480401674, 480400205, 480398782, 480397360, 480395937, 480394514, 480393091, 480391669, 480390246, 480388823, 480387400, 480385778, 480384153, 480382529, 480380904, 480379280, 480377656, 480376031, 480374407, 480372782, 480371158, 480369534, 480367909, 480366285, 480364660, 480363036, 480361412, 480359787, 480358163, 480356538, 480354914, 480353290, 480351665, 480350041, 480348416, 480346792, 480345168, 480343543, 480341919, 480340294, 480338670, 480337046, 480335421, 480333797, 480332172, 480330548, 480328924, 480327299, 480325675, 480324050, 480322426, 480320802, 480319177)
//    time.indices.foreach{
//      i =>
//        val interaction = if(time(i) < 170000){
//          Random.nextInt(100) < 20
//        }else{false}
//        val snapshot = new LogicalExecutionSnapshot(i.toString,interaction, time(i))
//        snapshot.checkpointCost = cost(i)
//        history.addSnapshot(time(i), snapshot)
//    }
    val planner = new ReplayCheckpointPlanner(history, 5000)
    val timelimits = Array(5000, 10000, 20000, 30000, 60000)
    timelimits.foreach{
      timelimit =>
        println("tau = "+timelimit+"---------------")
        var last = 0L
        var idx = 0
        val chkptset = mutable.ArrayBuffer[Int]()
        history.historyArray.foreach {
          i =>
            if(history.getSnapshot(i).isInteraction){
              val to_chkpt = i - last > timelimit
              if (to_chkpt) {
                last = i
                chkptset.append(idx)
              }
            }
            idx += 1
        }
        val plan3 = planner.getGlobalPlan(0, history.historyArray.length, timelimit)
        println(plan3.map(i => {
          history.getPlanCost(i)
        }).sum)
        println(chkptset.map(i => {
          history.getPlanCost(i)
        }).sum)
        println((0 until idx).map(i => {
          history.getPlanCost(i)
        }).sum)
    }
  }
}

class JsonTest {}
