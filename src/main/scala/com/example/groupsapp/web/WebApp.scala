package com.example.groupsapp.web

import akka.http.scaladsl.server.{HttpApp, Route}

object WebApp extends HttpApp {

  override def routes: Route =
    pathPrefix("user" / IntNumber) { userId =>
      path("groups") {
        get {
          complete(s"Getting groups: $userId")
        }
      } ~
        path("join" / IntNumber) { groupId =>
          put {
            complete(s"Joined group: $groupId")
          }
        } ~
        path("feeds") {
          get {
            complete(s"Getting all feeds")
          }
        } ~
        path("feed" / IntNumber) { groupId =>
          complete(s"Getting all feeds")
        }
    }
}
