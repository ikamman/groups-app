package com.example.groupsapp

import akka.actor.ActorSystem
import com.example.groupsapp.group.GroupSharding
import com.example.groupsapp.web.WebApp
import com.typesafe.config.ConfigFactory

object GroupsApp {

  def main(args: Array[String]): Unit = {
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
    WebApp.startServer("127.0.0.1", 8080, system)
  }

  def nodesStartup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory
        .parseString("akka.remote.artery.canonical.port=" + port)
        .withFallback(
          ConfigFactory.parseString("akka.cluster.roles=[\"group\"]")
        )
        .withFallback(ConfigFactory.load())
      val system = ActorSystem("GroupsSystem", config)

      system.actorOf(GroupSharding.props)
    }
  }
}
