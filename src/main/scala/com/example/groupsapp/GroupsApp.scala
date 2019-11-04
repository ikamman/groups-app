package com.example.groupsapp

import akka.actor.{ActorSystem, Props}
import com.example.groupsapp.user.UserShard
import com.typesafe.config.ConfigFactory

object GroupsApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551", "2552", "0"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory
        .parseString("akka.remote.artery.canonical.port=" + port)
        .withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ShardingSystem", config)
      // Create an actor that starts the sharding and sends random messages
      system.actorOf(UserShard.props)
    }
  }

}
