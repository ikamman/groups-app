package com.example.groupsapp.group

import com.example.groupsapp.group.Group.{Message, Subscription}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

trait SubscriptionRepository {
  def save(sub: Subscription): Future[Unit]
}

object LogSubscriptionRepository extends SubscriptionRepository with LazyLogging {
  override def save(msg: Subscription): Future[Unit] =
    Future.successful(logger.info(s"Saving subscription: $msg"))
}
