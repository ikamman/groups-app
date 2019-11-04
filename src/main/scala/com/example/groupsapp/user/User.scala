package com.example.groupsapp.user

import akka.actor.{Actor, ActorLogging}
import com.example.groupsapp.user.User.{
  AddGroup,
  Create,
  GetGroups,
  GetUser,
  UserGroups,
  UserState
}

object User {
  sealed trait UserCommand {
    val userId: Int
  }
  case class Create(userId: Int, name: String) extends UserCommand
  case class AddGroup(userId: Int, groupId: Int) extends UserCommand
  case class GetGroups(userId: Int) extends UserCommand
  case class GetUser(userId: Int) extends UserCommand

  case class UserGroups(groups: List[Int])

  case class UserState(userId: Int, name: String, groups: List[Int] = Nil)
}

class User extends Actor with ActorLogging {
  override def receive: Receive = inactive

  def inactive: Receive = {
    case Create(userId, name) =>
      context.become(active(UserState(userId, name)))
  }

  def active(state: UserState): Receive = {
    case AddGroup(_, groupId) =>
      context.become(active(state.copy(groups = state.groups :+ groupId)))
    case GetGroups(_) => sender ! UserGroups(state.groups)
    case GetUser(_)   => sender ! state
  }
}
