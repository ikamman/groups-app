package com.example.groupsapp.group

import akka.actor._
import akka.cluster.sharding._
import com.example.groupsapp.group.Group.{JoinGroup, PostMessage}

object GroupSharding {

  private val numberOfShards = 100

  val extractShardId: ShardRegion.ExtractShardId = {
    case JoinGroup(id, _, _)         => (id % numberOfShards).toString
    case PostMessage(id, _, _)       => (id % numberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      // StartEntity is used by remembering entities feature
      (id.toLong % numberOfShards).toString
  }

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg @ JoinGroup(id, _, _)   ⇒ (id.toString, msg)
    case msg @ PostMessage(id, _, _) => (id.toString, msg)
  }

  def start(feedRepo: FeedRepository, subsRepo: SubscriptionRepository)(implicit system: ActorSystem): ActorRef =
    ClusterSharding(system).start(
      typeName = "Group",
      entityProps = Props(new Group(feedRepo, subsRepo)),
      settings = ClusterShardingSettings(system).withRole("group"),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )
}
