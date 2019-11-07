package com.example.groupsapp.group

import com.example.groupsapp.DbProvider
import com.example.groupsapp.group.Group.{Message, Subscription}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.Try

trait FeedRepository {
  def save(msg: Message): Future[Unit]
  def findNByGroupId(groupId: Int, limit: Int): Future[List[Message]]
  def findNByGroupIdSince(groupId: Int, since: Long, limit: Int): Future[List[Message]]
  def findNByAllUserGroups(userId: Int, limit: Int): Future[List[Message]]
  def findNByAllUserGroupsSince(userId: Int, since: Long, limit: Int): Future[List[Message]]
}

trait FeedRepositoryProvider {
  val feedRepo: FeedRepository
}

trait DefaultFeedRepositoryProvider extends FeedRepositoryProvider with DbProvider with LazyLogging {

  import ctx._

  override val feedRepo: FeedRepository = new FeedRepository {

    override def save(msg: Message): Future[Unit] =
      Future.fromTry(Try {
        ctx.run(quote {
          query[Message].insert(lift(msg))
        })
      })

    override def findNByGroupId(groupId: Int, limit: Int): Future[List[Message]] =
      findNByGroupIdSince(groupId, 0, limit)

    override def findNByGroupIdSince(groupId: Int, since: Long, limit: Int): Future[List[Message]] =
      Future
        .fromTry(Try {
          ctx.run(quote {
            query[Message]
              .filter(_.groupId == lift(groupId))
              .filter(_.created > lift(since))
              .sortBy(_.created)(Ord.desc)
              .take(lift(limit))
          })
        })

    override def findNByAllUserGroups(userId: Int, limit: Int): Future[List[Message]] =
      findNByAllUserGroupsSince(userId, 0, limit)

    override def findNByAllUserGroupsSince(userId: Int, since: Long, limit: Int): Future[List[Message]] =
      Future
        .fromTry(Try {
          ctx.run(quote {
            query[Message]
              .filter(_.created > lift(since))
              .join(query[Subscription].filter(_.userId == lift(userId)))
              .on(_.groupId == _.groupId)
              .map(_._1)
              .sortBy(_.created)(Ord.desc)
              .take(lift(limit))
          })
        })
  }
}

object DefaultFeedRepositoryProvider extends DefaultFeedRepositoryProvider
