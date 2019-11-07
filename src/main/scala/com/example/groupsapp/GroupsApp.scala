package com.example.groupsapp

import akka.actor.ActorSystem
import com.example.groupsapp.group.{DefaultFeedRepositoryProvider, DefaultSubscriptionRepositoryProvider, GroupSharding}
import com.example.groupsapp.web.WebApp
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object GroupsApp extends LazyLogging {

  def main(args: Array[String]): Unit = {

    DbServerRunner.run()

    if (args.isEmpty) {
      nodesStartup(Seq("2551", "2552", "0"))
    } else {
      nodesStartup(args)
    }
    webServerStartup()
  }

  def webServerStartup(): Unit = {
    val config = ConfigFactory
      .parseString("akka.cluster.roles=[\"web\"]")
      .withFallback(ConfigFactory.load())
    val system = ActorSystem("GroupsSystem", config)
    new WebApp(system).startServer("127.0.0.1", 8080, system)
  }

  def nodesStartup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory
        .parseString("akka.remote.artery.canonical.port=" + port)
        .withFallback(
          ConfigFactory.parseString("akka.cluster.roles=[\"group\"]")
        )
        .withFallback(ConfigFactory.load())
      implicit val system: ActorSystem = ActorSystem("GroupsSystem", config)

      GroupSharding.start(DefaultFeedRepositoryProvider.feedRepo, DefaultSubscriptionRepositoryProvider.subsRepo)
    }
  }
}
