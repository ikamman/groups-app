package com.example.groupsapp.group

import akka.actor.{Actor, ActorContext, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import com.example.groupsapp.group.Group.GroupMessage
import com.example.groupsapp.group.GroupSubscriberActor.SubscribeAndSource

trait GroupSubscriber {

  implicit val context: ActorContext

  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def subscribeGroup(groupId: Int): Unit =
    mediator ! Subscribe(groupId.toString, context.self)
}

class GroupSubscriberActor(source: ActorRef) extends Actor with GroupSubscriber {

  context.watch(source)

  override def receive: Receive = {
    case SubscribeAndSource(groupId) =>
      subscribeGroup(groupId)
    case msg: GroupMessage => source forward msg
  }
}

object GroupSubscriberActor {
  case class SubscribeAndSource(groupId: Int)
}
