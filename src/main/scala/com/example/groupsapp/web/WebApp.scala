package com.example.groupsapp.web

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import com.example.groupsapp.group.Group.{AllGood, Message, NotSoGood, Reply, Subscription}
import com.example.groupsapp.group.{DefaultFeedRepositoryProvider, DefaultSubscriptionRepositoryProvider, GroupService}
import spray.json._

class WebApp(val system: ActorSystem)
    extends HttpApp
    with GroupService
    with DefaultSubscriptionRepositoryProvider
    with DefaultFeedRepositoryProvider {

  case class JoinRequest(userName: String)
  case class PostRequest(msg: String)

  object GroupJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val joinReqFormats: RootJsonFormat[JoinRequest] = jsonFormat1(JoinRequest)
    implicit val postReqFormats: RootJsonFormat[PostRequest] = jsonFormat1(PostRequest)
    implicit val subsFormats: RootJsonFormat[Subscription]   = jsonFormat3(Subscription)
    implicit val feedFormats: RootJsonFormat[Message]        = jsonFormat6(Message)
  }

  import GroupJsonProtocol._

  implicit override def routes: Route =
    pathPrefix("user" / IntNumber) { userId =>
      path("groups") {
        get {
          complete(getUserSubs(userId))
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
            complete(getLast20PostsAllFeeds(userId))
          }
        } ~
        path("feed" / IntNumber) { groupId =>
          complete(getLast20Posts(userId, groupId))
        }
    }

  def replyToResponse(msg: String)(reply: Reply): HttpResponse = reply match {
    case AllGood           => HttpResponse(StatusCodes.OK, Nil, msg)
    case NotSoGood(errMsg) => HttpResponse(StatusCodes.BadRequest, Nil, errMsg)
  }
}
