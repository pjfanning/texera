package edu.uci.ics.texera.web

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.TexeraWebApplication.parseArgs
import edu.uci.ics.texera.web.auth.JwtAuth.jwtConsumer
import edu.uci.ics.texera.web.auth.{GuestAuthFilter, SessionUser, UserAuthenticator, UserRoleAuthorizer}
import edu.uci.ics.texera.web.resource.{SchemaPropagationResource, SystemMetadataResource, WorkflowEditingResource}
import io.dropwizard.Configuration
import io.dropwizard.auth.{AuthDynamicFeature, AuthValueFactoryProvider}
import io.dropwizard.setup.{Bootstrap, Environment}
import org.eclipse.jetty.server.session.SessionHandler
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature

object TexeraWorkflowEditingApplication {
  def main(args: Array[String]): Unit = {
    val argMap = parseArgs(args)

    new TexeraWorkflowEditingApplication().run(
      "server",
      Utils.amberHomePath
        .resolve("src")
        .resolve("main")
        .resolve("resources")
        .resolve("texera-editing-web-config.yml")
        .toString
    )
  }
}

class TexeraWorkflowEditingApplication extends io.dropwizard.Application[TexeraWorkflowEditingWebConfiguration] with LazyLogging{
  override def initialize(bootstrap: Bootstrap[TexeraWorkflowEditingWebConfiguration]): Unit = {
    // register scala module to dropwizard default object mapper
    bootstrap.getObjectMapper.registerModule(DefaultScalaModule)
  }

  override def run(configuration: TexeraWorkflowEditingWebConfiguration, environment: Environment): Unit = {
    // serve backend at /api/texera
    environment.jersey.setUrlPattern("/api/texera/*")

    // register SessionHandler
    environment.jersey.register(classOf[SessionHandler])
    environment.servlets.setSessionHandler(new SessionHandler)
    environment.jersey.register(classOf[WorkflowEditingResource])

    if (AmberConfig.isUserSystemEnabled) {
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
  }
}
