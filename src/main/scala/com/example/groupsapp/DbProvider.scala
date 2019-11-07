package com.example.groupsapp

import io.getquill.{H2JdbcContext, SnakeCase}

trait DbProvider {
  lazy val ctx = new H2JdbcContext(SnakeCase, "db.ctx")

}
