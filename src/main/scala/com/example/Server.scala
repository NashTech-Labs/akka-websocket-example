package com.example

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

object Server extends App with SprayJsonSupport with DefaultJsonProtocol
{
    implicit val system: ActorSystem = ActorSystem("websocket-with-akka")
    implicit val executor: ExecutionContextExecutor = system.dispatcher
    val webSocketRoute =
        get
        {
            handleWebSocketMessages(getWordPalindrome)
        }

    def getWordPalindrome: Flow[Message, Message, NotUsed] =
        Flow[Message].collect
        {
            case TextMessage.Strict(textMessage) => TextMessage(textMessage.reverse)
            case BinaryMessage.Strict(binaryMessage) => BinaryMessage(binaryMessage.reverse)
        }

    Http().newServerAt("localhost", 8080).bind(webSocketRoute).map
    { _ =>
        println(s"Server is running at http://localhost:8080/")
    }
}
