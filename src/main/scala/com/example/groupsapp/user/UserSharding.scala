package com.example.groupsapp.user

import akka.actor._
import akka.cluster.sharding._

object UserSharding {
  def props: Props = Props[UserSharding]
}

class UserSharding extends Actor with ActorLogging {

  import User._

  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg: UserCommand => (msg.userId.toString, msg)
  }

  private val numberOfShards = 100

  private val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: UserCommand => (cmd.userId % numberOfShards).toString
  }

  val groupRegion: ActorRef = ClusterSharding(context.system).start(
    typeName = "User",
    entityProps = Props[User],
    settings = ClusterShardingSettings(context.system).withRole("user"),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  def receive = {
    case msg: UserCommand =>
      log.info(s"Sending $msg");
      groupRegion ! msg
  }
}
