package com.example.groupsapp

import akka.actor.ActorSystem
import com.example.groupsapp.group.GroupSharding
import com.example.groupsapp.user.UserSharding
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
      val config = ConfigFactory
        .parseString("akka.remote.artery.canonical.port=" + port)
        .withFallback(ConfigFactory.load())

      val system = ActorSystem("GroupsSystem", config)
      val userSharding = system.actorOf(UserSharding.props)
      system.actorOf(GroupSharding.props(userSharding))
    }
  }

}
