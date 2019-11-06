package com.example.groupsapp.group

import akka.actor.ActorContext
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe

trait GroupSubscriber {

  implicit val context: ActorContext

  val mediator = DistributedPubSub(context.system).mediator

  def subscribeGroup(groupId: Int): Unit =
    mediator ! Subscribe(groupId.toString, context.self)
}
