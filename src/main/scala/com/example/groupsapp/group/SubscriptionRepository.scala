package com.example.groupsapp.group

import com.example.groupsapp.DbProvider
import com.example.groupsapp.group.Group.Subscription
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.Try

trait SubscriptionRepository {
  def save(sub: Subscription): Future[Unit]
  def findUserSubs(userId: Int): Future[List[Subscription]]
  def memberOf(userId: Int, groupId: Int): Future[Boolean]
}

trait SubscriptionRepositoryProvider {
  val subsRepo: SubscriptionRepository
}

trait DefaultSubscriptionRepositoryProvider extends SubscriptionRepositoryProvider with DbProvider {

  import ctx._

  val subsRepo: SubscriptionRepository = new SubscriptionRepository with LazyLogging {
    override def save(msg: Subscription): Future[Unit] = {
      Future.fromTry(Try {
        ctx.run(quote {
          query[Subscription].insert(lift(msg))
        })
      })
    }

    override def findUserSubs(userId: Int): Future[List[Subscription]] = {
      Future.fromTry(Try {
        ctx.run(quote {
          query[Subscription].filter(_.userId == lift(userId))
        })
      })
    }
    override def memberOf(userId: Int, groupId: Int): Future[Boolean] =
      Future.fromTry(Try {
        ctx.run(quote {
          query[Subscription]
            .filter(_.userId == lift(userId))
            .filter(_.groupId == lift(groupId))
            .nonEmpty
        })
      })

  }
}

object DefaultSubscriptionRepositoryProvider extends DefaultSubscriptionRepositoryProvider
