package com.example.groupsapp

import java.sql.DriverManager

import com.typesafe.config.ConfigFactory

object DbServerRunner {
  def run(): Unit = {
    val config   = ConfigFactory.load().getConfig("db.ctx.dataSource")
    val h2Server = org.h2.tools.Server.createTcpServer().start()
    // making sure database is initialized prior first use
    val connection = DriverManager.getConnection(config.getString("url"), config.getString("user"), "")
    connection.close()
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        h2Server.stop()
      }
    })
  }
}
