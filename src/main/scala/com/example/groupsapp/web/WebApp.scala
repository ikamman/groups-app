package com.example.groupsapp.web

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import com.example.groupsapp.group.Group.{AllGood, NotSoGood, Reply}
import com.example.groupsapp.group.{GroupService, LogSubscriptionRepositoryProvider}
import spray.json._

class WebApp(val system: ActorSystem) extends HttpApp with GroupService with LogSubscriptionRepositoryProvider {

  case class JoinRequest(userName: String)
  case class PostRequest(msg: String)
  object GroupJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val joinReqFormats: RootJsonFormat[JoinRequest] = jsonFormat1(JoinRequest)
    implicit val postReqFormats: RootJsonFormat[PostRequest] = jsonFormat1(PostRequest)
  }

  import GroupJsonProtocol._

  implicit override def routes: Route =
    pathPrefix("user" / IntNumber) { userId =>
      path("groups") {
        get {
          complete(s"Getting groups: $userId")
        }
      } ~
        path("join" / IntNumber) { groupId =>
          post {
            entity(as[JoinRequest]) { req =>
              complete(joinGroup(groupId, userId, req.userName)
                .map(replyToResponse("Enjoy")))
            }
          }
        } ~
        path("post" / IntNumber) { groupId =>
          post {
            entity(as[PostRequest]) { req =>
              complete(sendMessage(groupId, userId, req.msg)
                .map(replyToResponse("Message sent")))
            }
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

  def replyToResponse(msg: String)(reply: Reply): HttpResponse = reply match {
    case AllGood           => HttpResponse(StatusCodes.OK, Nil, msg)
    case NotSoGood(errMsg) => HttpResponse(StatusCodes.BadRequest, Nil, errMsg)
  }
}
