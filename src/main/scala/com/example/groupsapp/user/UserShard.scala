package com.example.groupsapp.user

import akka.actor._
import akka.cluster.sharding._
import com.example.groupsapp.user.User._

object UserShard {
  def props: Props = Props[UserShard]
}

class UserShard extends Actor with ActorLogging {

  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg: UserCommand => (msg.userId.toString, msg)
  }

  private val numberOfShards = 100

  private val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: UserCommand => (cmd.userId % numberOfShards).toString
  }

  val deviceRegion: ActorRef = ClusterSharding(context.system).start(
    typeName = "User",
    entityProps = Props[User],
    settings = ClusterShardingSettings(context.system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  def receive = {
    case msg: UserCommand =>
      log.info(s"Sending $msg");
      deviceRegion ! msg
  }
}
