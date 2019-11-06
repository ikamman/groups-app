package com.example.groupsapp.group

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.example.groupsapp.user.User.AddGroup

object Group {

  case class Message(id: String,
                     groupId: Int,
                     userId: Int,
                     msg: String,
                     create: Long)

  case class Subscription(groupId: Int, userId: Int)

  // Commands
  sealed trait GroupCommand {
    val groupId: Int
  }
  case class Create(groupId: Int) extends GroupCommand
  case class AddMember(groupId: Int, userId: Int) extends GroupCommand
  case class PostMessage(groupId: Int, userId: Int, msg: String)
      extends GroupCommand

  // State
  case class GroupState(groupId: Int, members: List[Int] = Nil)
}

class Group(user: ActorRef, feedRepo: GroupFeedRepository)
    extends Actor
    with ActorLogging {

  import Group._

  val mediator = DistributedPubSub(context.system).mediator

  override def receive: Receive = inactive

  def inactive: Receive = {
    case Create(id) =>
      context.become(active(GroupState(id)))
  }

  def active(state: GroupState): Receive = {
    case AddMember(groupId, userId) =>
      user ! AddGroup(userId, groupId)
      context.become(active(state.copy(members = state.members :+ userId)))
    case PostMessage(groupId, userId, message) =>
      val timeStamp = System.currentTimeMillis()
      val messageToSend = Message(
        s"$groupId$userId$timeStamp",
        groupId,
        userId,
        message,
        timeStamp
      )
      feedRepo.save(messageToSend)
      mediator ! Publish(groupId.toString, message)
  }
}
