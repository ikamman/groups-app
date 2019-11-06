package com.example.groupsapp.user

import akka.actor.{Actor, ActorLogging, Props}
import com.example.groupsapp.group.Group.Message

object User {

  // Commands
  sealed trait UserCommand {
    val userId: Int
  }
  case class Init(userId: Int, name: String) extends UserCommand
  case class AddGroup(userId: Int, groupId: Int) extends UserCommand
  case class GetGroups(userId: Int) extends UserCommand
  case class GetUser(userId: Int) extends UserCommand

  // State
  case class UserGroups(groups: List[Int])
  case class UserState(userId: Int, name: String, groups: List[Int] = Nil)
}

class User extends Actor with GroupSubscriber with ActorLogging {

  import User._

  override def receive: Receive = inactive

  def inactive: Receive = {
    case Init(userId, name) =>
      context.become(active(UserState(userId, name)))
  }

  def active(state: UserState): Receive = {
    case AddGroup(userId, groupId) =>
      log.info(s"Adding $userId to group $groupId")
      subscribeGroup(groupId)
      context.become(active(state.copy(groups = state.groups :+ groupId)))
    case GetGroups(_) => sender ! UserGroups(state.groups)
    case GetUser(_)   => sender ! state
    case msg: Message =>
      log.info(s"Received message from group: $msg")
  }
}
