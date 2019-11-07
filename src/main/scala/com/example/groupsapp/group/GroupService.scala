package com.example.groupsapp.group

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.cluster.sharding.ClusterSharding
import akka.util.Timeout
import com.example.groupsapp.group.Group.{JoinGroup, PostMessage, Reply}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait GroupService extends SubscriptionRepositoryProvider with FeedRepositoryProvider {
  implicit val system: ActorSystem
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout     = Timeout(15 seconds)

  val groupRegionProxy: ActorRef = ClusterSharding(system).startProxy("Group",
                                                                      Some("group"),
                                                                      GroupSharding.extractEntityId,
                                                                      GroupSharding.extractShardId)

  def joinGroup(groupId: Int, userId: Int, userName: String): Future[Reply] =
    (groupRegionProxy ? JoinGroup(groupId, userId, userName)).map(_.asInstanceOf[Reply])

  def getUserSubs(userId: Int): Future[List[Int]] =
    subsRepo.findUserSubs(userId).map(_.map(_.groupId))

  def sendMessage(groupId: Int, userId: Int, msg: String): Future[Reply] =
    (groupRegionProxy ? PostMessage(groupId, userId, msg)).map(_.asInstanceOf[Reply])

  def getLast20Posts(userId: Int, groupId: Int): Future[List[Group.GroupMessage]] =
    for {
      isMember <- subsRepo.memberOf(userId, groupId)
      list     <- feedRepo.findNByGroupId(groupId, 20) if isMember
    } yield list.sortBy(_.created)

  def getLast20PostsAllFeeds(userId: Int): Future[List[Group.GroupMessage]] =
    feedRepo.findNByAllUserGroups(userId, 20).map(_.sortBy(_.created))

}
