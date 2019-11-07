package com.example.groupsapp.web

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.javadsl.model.headers.SecWebSocketProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.example.groupsapp.group.Group.{AllGood, GroupMessage, NotSoGood, Reply, Subscription}
import com.example.groupsapp.group.GroupSubscriberActor.SubscribeAndSource
import com.example.groupsapp.group.{
  DefaultFeedRepositoryProvider,
  DefaultSubscriptionRepositoryProvider,
  GroupService,
  GroupSubscriberActor
}
import spray.json._

class WebApp(implicit val system: ActorSystem)
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
    implicit val feedFormats: RootJsonFormat[GroupMessage]   = jsonFormat6(GroupMessage)
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
        pathPrefix("feeds") {
          get {
            pathEndOrSingleSlash {
              complete(getLast20PostsAllFeeds(userId))
            } ~ path("stream") {
              val (source, flow) = wsSourceAndFlow
              subsRepo.findUserSubs(userId).map(_.foreach(sub => source ! SubscribeAndSource(sub.groupId)))
              handleWebSocketMessages(
                flow
                  .prepend(Source.fromFuture(feedRepo.findNByAllUserGroups(userId, 20)).mapConcat(identity))
                  .map(msg => TextMessage(feedFormats.write(msg).prettyPrint)))
            }
          }
        } ~
        pathPrefix("feed" / IntNumber) { groupId =>
          get {
            pathEndOrSingleSlash {
              complete(getLast20Posts(userId, groupId))
            } ~
              path("stream") {
                val (source, flow) = wsSourceAndFlow
                source ! SubscribeAndSource(groupId)
                handleWebSocketMessages(
                  flow
                    .prepend(Source.fromFuture(feedRepo.findNByGroupId(groupId, 20)).mapConcat(identity))
                    .map(msg => TextMessage(feedFormats.write(msg).prettyPrint)))
              }

          }
        }
    }

  def replyToResponse(msg: String)(reply: Reply): HttpResponse = reply match {
    case AllGood           => HttpResponse(StatusCodes.OK, Nil, msg)
    case NotSoGood(errMsg) => HttpResponse(StatusCodes.BadRequest, Nil, errMsg)
  }

  def wsSourceAndFlow(implicit system: ActorSystem): (ActorRef, Flow[Message, GroupMessage, NotUsed]) = {

    implicit val materialize: ActorMaterializer = ActorMaterializer()

    val (sourceRef, source) = Source
      .actorRef[GroupMessage](100, OverflowStrategy.fail)
      .preMaterialize

    (system.actorOf(Props(new GroupSubscriberActor(sourceRef))), Flow.fromSinkAndSource(Sink.ignore, source))
  }
}
