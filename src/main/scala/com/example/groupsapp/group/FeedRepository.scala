package com.example.groupsapp.group

import com.example.groupsapp.group.Group.Message
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

trait FeedRepository {
  def save(msg: Message): Future[Unit]
}

object LogFeedRepository extends FeedRepository with LazyLogging {
  override def save(msg: Message): Future[Unit] =
    Future.successful(logger.info(s"Saving message: $msg"))
}
