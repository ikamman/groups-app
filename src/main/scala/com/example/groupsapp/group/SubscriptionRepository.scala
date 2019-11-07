package com.example.groupsapp.group

import com.example.groupsapp.group.Group.{Message, Subscription}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

trait SubscriptionRepository {
  def save(sub: Subscription): Future[Unit]
  def findUserSubs(userId: Int): Future[List[Subscription]]
}

trait SubscriptionRepositoryProvider {
  val subsRepo: SubscriptionRepository
}

trait LogSubscriptionRepositoryProvider extends SubscriptionRepositoryProvider {

  val subsRepo: SubscriptionRepository = new SubscriptionRepository with LazyLogging {
    override def save(msg: Subscription): Future[Unit] =
      Future.successful(logger.info(s"Saving subscription: $msg"))

    override def findUserSubs(userId: Int): Future[List[Subscription]] = ???
  }
}

object LogSubscriptionRepositoryProvider extends LogSubscriptionRepositoryProvider
