package edu.uci.ics.texera.web

import akka.actor.{ActorSystem, Cancellable}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.dirkraft.dropwizard.fileassets.FileAssetsBundle
import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Indexes
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoCursor, MongoDatabase}
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.auth.JwtAuth.jwtConsumer
import edu.uci.ics.texera.web.auth.{
  GuestAuthFilter,
  SessionUser,
  UserAuthenticator,
  UserRoleAuthorizer
}
import edu.uci.ics.texera.web.resource.auth.{AuthResource, GoogleAuthResource}
import edu.uci.ics.texera.web.resource.dashboard.file.{UserFileAccessResource, UserFileResource}
import edu.uci.ics.texera.web.resource.dashboard.workflow.{
  WorkflowAccessResource,
  WorkflowExecutionsResource,
  WorkflowResource,
  WorkflowVersionResource
}
import edu.uci.ics.texera.web.resource.dashboard.project.ProjectResource
import edu.uci.ics.texera.web.resource._
import io.dropwizard.auth.{AuthDynamicFeature, AuthValueFactoryProvider}
import io.dropwizard.setup.{Bootstrap, Environment}
import io.dropwizard.websockets.WebsocketBundle
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ErrorPageErrorHandler
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import java.time.Duration
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.web.TexeraWebApplication.scheduleRecurringCallThroughActorSystem
import org.apache.commons.jcs3.access.exception.InvalidArgumentException
import org.bson.Document

import java.util.concurrent.TimeUnit
import scala.annotation.tailrec

object TexeraWebApplication {

  def createAmberRuntime(
      workflow: Workflow,
      conf: ControllerConfig,
      errorHandler: Throwable => Unit
  ): AmberClient = {
    new AmberClient(actorSystem, workflow, conf, errorHandler)
  }

  def scheduleCallThroughActorSystem(delay: FiniteDuration)(call: => Unit): Cancellable = {
    actorSystem.scheduler.scheduleOnce(delay)(call)
  }

  def scheduleRecurringCallThroughActorSystem(initialDelay: FiniteDuration, delay: FiniteDuration)(
      call: => Unit
  ): Cancellable = {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay, delay)(() => call)
  }

  private var actorSystem: ActorSystem = _

  type OptionMap = Map[Symbol, Any]
  def parseArgs(args: Array[String]): OptionMap = {
    @tailrec
    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      list match {
        case Nil => map
        case "--cluster" :: value :: tail =>
          nextOption(map ++ Map('cluster -> value.toBoolean), tail)
        case option :: tail =>
          throw new InvalidArgumentException("unknown command-line arg")
      }
    }

    nextOption(Map(), args.toList)
  }

  def main(args: Array[String]): Unit = {
    val argMap = parseArgs(args)

    val clusterMode = argMap.get('cluster).asInstanceOf[Option[Boolean]].getOrElse(false)

    // start actor system master node
    actorSystem = AmberUtils.startActorMaster(clusterMode)

    // start web server
    new TexeraWebApplication().run(
      "server",
      Utils.amberHomePath
        .resolve("src")
        .resolve("main")
        .resolve("resources")
        .resolve("web-config.yml")
        .toString
    )
  }
}

class TexeraWebApplication extends io.dropwizard.Application[TexeraWebConfiguration] {

  override def initialize(bootstrap: Bootstrap[TexeraWebConfiguration]): Unit = {
    // serve static frontend GUI files
    bootstrap.addBundle(new FileAssetsBundle("../new-gui/dist", "/", "index.html"))
    // add websocket bundle
    bootstrap.addBundle(new WebsocketBundle(classOf[WorkflowWebsocketResource]))
    bootstrap.addBundle(new WebsocketBundle(classOf[CollaborationResource]))
    // register scala module to dropwizard default object mapper
    bootstrap.getObjectMapper.registerModule(DefaultScalaModule)
  }

  override def run(configuration: TexeraWebConfiguration, environment: Environment): Unit = {
    // serve backend at /api
    environment.jersey.setUrlPattern("/api/*")

    // redirect all 404 to index page, according to Angular routing requirements
    val eph = new ErrorPageErrorHandler
    eph.addErrorPage(404, "/")
    environment.getApplicationContext.setErrorHandler(eph)

    val webSocketUpgradeFilter =
      WebSocketUpgradeFilter.configureContext(environment.getApplicationContext)
    webSocketUpgradeFilter.getFactory.getPolicy.setIdleTimeout(Duration.ofHours(1).toMillis)
    environment.getApplicationContext.setAttribute(
      classOf[WebSocketUpgradeFilter].getName,
      webSocketUpgradeFilter
    )

    // register SessionHandler
    environment.jersey.register(classOf[SessionHandler])
    environment.servlets.setSessionHandler(new SessionHandler)

    // register MultiPartFeature
    environment.jersey.register(classOf[MultiPartFeature])

    environment.jersey.register(classOf[SystemMetadataResource])
    // environment.jersey().register(classOf[MockKillWorkerResource])
    environment.jersey.register(classOf[SchemaPropagationResource])

    if (AmberUtils.amberConfig.getBoolean("user-sys.enabled")) {
      // register JWT Auth layer
      environment.jersey.register(
        new AuthDynamicFeature(
          new JwtAuthFilter.Builder[SessionUser]()
            .setJwtConsumer(jwtConsumer)
            .setRealm("realm")
            .setPrefix("Bearer")
            .setAuthenticator(UserAuthenticator)
            .setAuthorizer(UserRoleAuthorizer)
            .buildAuthFilter()
        )
      )
    } else {
      // register Guest Auth layer
      environment.jersey.register(
        new AuthDynamicFeature(
          new GuestAuthFilter.Builder().setAuthorizer(UserRoleAuthorizer).buildAuthFilter()
        )
      )
    }

    environment.jersey.register(
      new AuthValueFactoryProvider.Binder[SessionUser](classOf[SessionUser])
    )
    environment.jersey.register(classOf[RolesAllowedDynamicFeature])

    environment.jersey.register(classOf[AuthResource])
    environment.jersey.register(classOf[GoogleAuthResource])
    environment.jersey.register(classOf[UserConfigResource])
    environment.jersey.register(classOf[UserFileAccessResource])
    environment.jersey.register(classOf[UserFileResource])
    environment.jersey.register(classOf[WorkflowAccessResource])
    environment.jersey.register(classOf[WorkflowResource])
    environment.jersey.register(classOf[WorkflowVersionResource])
    environment.jersey.register(classOf[ProjectResource])
    environment.jersey.register(classOf[WorkflowExecutionsResource])

    // create a mongo collection that handles cleanup of other stored collections only when mode is mongodb
    if (AmberUtils.amberConfig.getString("storage.mode").equalsIgnoreCase("mongodb")) {
      // create the new catalog collection
      val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
      val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
      val client: MongoClient = MongoClients.create(url)
      val database: MongoDatabase = client.getDatabase(databaseName)
      val catalogCollectionName: String =
        AmberUtils.amberConfig.getString("storage.mongodb.cleanup.catalog-collection-name")
      val catalogCollection: MongoCollection[Document] =
        database.getCollection(catalogCollectionName)
      val timeToLive: Int = AmberUtils.amberConfig.getInt("storage.mongodb.cleanup.ttl-in-seconds")
      // if the collection doesn't exist, then create it and add an index on the timestamp column
      if (catalogCollection.countDocuments() == 0) {
        catalogCollection.createIndex(Indexes.ascending("createdAt"))
      }

      scheduleRecurringCallThroughActorSystem(
        2.seconds,
        AmberUtils.amberConfig
          .getInt("storage.mongodb.cleanup.collection-check-interval-in-seconds")
          .seconds
      ) {
        cleanOldCollections(database, catalogCollection, timeToLive)
      }

    }
  }

  /**
    * This function is called periodically and checks all expired collections and deletes them
    * MongoDB doesn't have an API of drop collection where collection name in (from a subquery), so the implementation is to retrieve
    * the entire list of those documents that have expired, then loop the list to drop them one by one
    */
  def cleanOldCollections(
      database: MongoDatabase,
      catalogCollection: MongoCollection[Document],
      timeToLive: Int
  ): Unit = {
    val condition: BasicDBObject = new BasicDBObject
    condition.put(
      "createdAt",
      new BasicDBObject("$lt", System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(timeToLive))
    )
    val expiredDocuments: MongoCursor[Document] = catalogCollection.find(condition).cursor()
    while (expiredDocuments.hasNext) {
      database.getCollection(expiredDocuments.next().getString("collectionName")).drop()
    }
  }
}
