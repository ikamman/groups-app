package com.example.groupsapp.group

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import monocle.macros.syntax.lens._

object Group {

  case class Message(id: String, groupId: Int, userId: Int, userName: String, message: String, created: Long)
  case class Subscription(groupId: Int, userId: Int, userName: String)

  // Commands
  sealed trait GroupCommand
  case class JoinGroup(groupId: Int, userId: Int, userName: String) extends GroupCommand
  case class PostMessage(groupId: Int, userId: Int, msg: String)    extends GroupCommand

  // Reply
  sealed trait Reply
  case object AllGood               extends Reply
  case class NotSoGood(msg: String) extends Reply

  // State
  case class GroupState(members: Map[Int, String] = Map.empty)
}

class Group(feedRepo: FeedRepository, subsRepo: SubscriptionRepository) extends Actor with ActorLogging {

  import Group._

  val publisher: ActorRef = DistributedPubSub(context.system).mediator

  override def receive: Receive = active(GroupState())

  def active(state: GroupState): Receive = {
    case JoinGroup(groupId, userId, userName) =>
      if (isMember(userId, state)) {
        sender() ! NotSoGood("User is already a member of this group")
      } else {
        val newState = state.lens(_.members).modify(_ + (userId -> userName))
        subsRepo.save(Subscription(groupId, userId, userName))
        sender() ! AllGood
        context.become(active(newState))
      }

    case PostMessage(groupId, userId, message) =>
      if (isMember(userId, state)) {
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
        sender() ! AllGood
      } else {
        sender() ! NotSoGood("Only members can post messages")
      }
  }

  def isMember(userId: Int, state: GroupState): Boolean = state.members.get(userId).isDefined
}
