package com.example.groupsapp.group

import akka.actor._
import akka.cluster.sharding._
import com.example.groupsapp.group.Group.GroupCommand

object GroupSharding {
  def props(user: ActorRef): Props = Props(new GroupSharding(user))
}

class GroupSharding(user: ActorRef) extends Actor with ActorLogging {

  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg: GroupCommand => (msg.groupId.toString, msg)
  }

  private val numberOfShards = 100

  private val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: GroupCommand => (cmd.groupId % numberOfShards).toString
  }

  val groupRegion: ActorRef = ClusterSharding(context.system).start(
    typeName = "Group",
    entityProps = Props(new Group(user, LogGroupFeedRepository)),
    settings = ClusterShardingSettings(context.system).withRole("group"),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  def receive = {
    case msg: GroupCommand =>
      log.info(s"Sending $msg");
      groupRegion ! msg
  }
}
