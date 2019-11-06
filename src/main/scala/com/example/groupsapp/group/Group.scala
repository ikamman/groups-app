package com.example.groupsapp.group

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import monocle.macros.syntax.lens._

object Group {

  case class Message(id: String, groupId: Int, userId: Int, userName: String, msg: String, create: Long)

  case class Subscription(groupId: Int, userId: Int, userName: String)

  // Commands
  sealed trait GroupCommand {
    val groupId: Int
  }
  case class Create(groupId: Int)                                   extends GroupCommand
  case class JoinGroup(groupId: Int, userId: Int, userName: String) extends GroupCommand
  case class PostMessage(groupId: Int, userId: Int, msg: String)    extends GroupCommand

  // State
  case class GroupState(members: Map[Int, String] = Map.empty)
}

class Group(feedRepo: FeedRepository, subsRepo: SubscriptionRepository) extends Actor with ActorLogging {

  import Group._

  val publisher: ActorRef = DistributedPubSub(context.system).mediator

  override def receive: Receive = active(GroupState())

  def active(state: GroupState): Receive = {
    case JoinGroup(_, userId, userName) =>
      val newState = state.lens(_.members).modify(_ + (userId -> userName))
      context.become(active(newState))

    case PostMessage(groupId, userId, message) =>
      val timestamp = System.currentTimeMillis()
      val messageToSend = Message(
        s"$groupId$userId$timestamp",
        groupId,
        userId,
        state.members(userId),
        message,
        timestamp
      )

      feedRepo.save(messageToSend)
      publisher ! Publish(groupId.toString, messageToSend)
  }
}
