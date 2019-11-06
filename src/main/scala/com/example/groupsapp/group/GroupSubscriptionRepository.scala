package com.example.groupsapp.group

import com.example.groupsapp.group.Group.Subscription

import scala.concurrent.Future

trait GroupSubscriptionRepository {
  def save(sub: Subscription): Future[Unit]
}
