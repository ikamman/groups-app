package com.example.groupsapp.group

import com.example.groupsapp.group.Group.Message
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

trait GroupFeedRepository {
  def save(msg: Message): Future[Unit]
}

object LogGroupFeedRepository extends GroupFeedRepository with LazyLogging {
  override def save(msg: Message): Future[Unit] =
    Future.successful(logger.info(s"Saving message: $msg"))
}
