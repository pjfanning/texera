package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.architecture.common.{Interaction, InteractionHistory, VirtualIdentityUtils}
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.service.ReplayPlanner
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING
import edu.uci.ics.texera.workflow.operators.nn.DNNOpExec

object JsonTest {

  def main(args: Array[String]): Unit = {

    val scan1 = VirtualIdentityUtils.createWorkerIdentity("","Scan","",1)
    val scan2 = VirtualIdentityUtils.createWorkerIdentity("","Scan","",2)
    val filter1 = VirtualIdentityUtils.createWorkerIdentity("","Filter","",1)
    val filter2 = VirtualIdentityUtils.createWorkerIdentity("","Filter","",2)
    val ml1 = VirtualIdentityUtils.createWorkerIdentity("","ML","",1)
    val ml2 = VirtualIdentityUtils.createWorkerIdentity("","ML","",2)
    val join1 = VirtualIdentityUtils.createWorkerIdentity("","Join","",1)
    val filter3 = VirtualIdentityUtils.createWorkerIdentity("","Filter","",3)

    val history = new InteractionHistory()

    history.addCompletion(scan1, 40)
    history.addCompletion(scan2, 40)
    history.addCompletion(filter1, 60)
    history.addCompletion(filter2, 60)
    history.addCompletion(ml1, 80)
    history.addCompletion(ml2, 80)
    history.addCompletion(join1, 100)
    history.addCompletion(filter3, 40)

    val interaction1 = new Interaction()
    interaction1.addParticipant(CONTROLLER, 100, 0, 0)
    interaction1.addParticipant(scan1, 20, 10,10)
    interaction1.addParticipant(filter1, 25, 10,0)
    interaction1.addParticipant(ml1, 30, 500, 500)
    interaction1.addParticipant(join1, 10, 10,10)

    val interaction2 = new Interaction()
    interaction2.addParticipant(CONTROLLER, 200, 0, 0)
    interaction2.addParticipant(scan1, 40, 10,10)
    interaction2.addParticipant(filter1, 55, 10,0)
    interaction2.addParticipant(ml1, 50, 500, 500)
    interaction2.addParticipant(join1, 20, 50,50)

    val interaction3 = new Interaction()
    interaction3.addParticipant(CONTROLLER, 300, 0, 0)
    interaction3.addParticipant(scan1, 60, 10,10)
    interaction3.addParticipant(filter1, 70, 10,0)
    interaction3.addParticipant(ml1, 70, 500, 500)
    interaction3.addParticipant(join1, 30, 100,100)

    val interaction4 = new Interaction()
    interaction4.addParticipant(CONTROLLER, 400, 0, 0)
    interaction4.addParticipant(scan1, 70, 10,10)
    interaction4.addParticipant(filter1, 80, 10,0)
    interaction4.addParticipant(ml1, 80, 500, 500)
    interaction4.addParticipant(join1, 30, 300,300)

    val interaction5 = new Interaction()
    interaction5.addParticipant(CONTROLLER, 500, 0, 0)
    interaction5.addParticipant(scan1, 80, 10,10)
    interaction5.addParticipant(filter1, 90, 10,0)
    interaction5.addParticipant(ml1, 100, 500, 500)
    interaction5.addParticipant(scan2, 30, 10,10)
    interaction5.addParticipant(filter2, 40, 10,0)
    interaction5.addParticipant(ml2, 50, 500, 500)
    interaction5.addParticipant(join1, 40, 301,301)
    interaction5.addParticipant(filter3, 30, 10,0)

    val interaction6 = new Interaction()
    interaction6.addParticipant(CONTROLLER, 500, 0, 0)
    interaction6.addParticipant(scan1, 80, 10,10)
    interaction6.addParticipant(filter1, 90, 10,0)
    interaction6.addParticipant(ml1, 100, 500, 500)
    interaction6.addParticipant(scan2, 60, 10,10)
    interaction6.addParticipant(filter2, 80, 10,0)
    interaction6.addParticipant(ml2, 70, 500, 500)
    interaction6.addParticipant(join1, 50, 301,301)
    interaction6.addParticipant(filter3, 50, 10,0)

    val interaction7 = new Interaction()
    interaction7.addParticipant(CONTROLLER, 500, 0, 0)
    interaction7.addParticipant(scan1, 80, 10,10)
    interaction7.addParticipant(filter1, 90, 10,0)
    interaction7.addParticipant(ml1, 100, 500, 500)
    interaction7.addParticipant(scan2, 60, 10,10)
    interaction7.addParticipant(filter2, 80, 10,0)
    interaction7.addParticipant(ml2, 70, 500, 500)
    interaction7.addParticipant(join1, 60, 10,10)
    interaction7.addParticipant(filter3, 80, 10,0)

    history.addInteraction(900,interaction1)
    history.addInteraction(2200,interaction2)
    history.addInteraction(3100,interaction3)
    history.addInteraction(3800,interaction4)
    history.addInteraction(4200,interaction5)
    history.addInteraction(5800,interaction6)
    history.addInteraction(7100,interaction7)

    val planner = new ReplayPlanner(history)

    // planner.startPlanning(7)
    println("best plan: "+planner.bruteForcePlanner(0,7, 3000))
    println("best plan2: "+planner.dynamicProgrammingPlanner(0,7, 3000))

    println("cost 1: "+history.getCheckpointCost(3, planner.getCheckpointMap(Array[Int]())))
    println("cost 2: "+history.getCheckpointCost(5, planner.getCheckpointMap(Array[Int]())))
    println("cost 1: "+history.getCheckpointCost(5, planner.getCheckpointMap(Array(3))))
    println("cost 2: "+history.getCheckpointCost(3, planner.getCheckpointMap(Array(5))))
    //println("best plan: "+planner.dynamicProgrammingPlanner(0,7, 4000))
  }
}

class JsonTest {}
