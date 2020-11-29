package com.example

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl._

import scala.concurrent.Future

object WebSocketClientFlow
{
    def main(args: Array[String]): Unit =
    {
        implicit val system: ActorSystem = ActorSystem()
        implicit val executor = system.dispatcher

        val incoming: Sink[Message, Future[Done]] =
            Sink.foreach[Message]
              {
                  case message: TextMessage.Strict =>
                      println(message.text)
                  case _ =>
              }

        val outgoing = Source.single(TextMessage(args(0 )))

        val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://localhost:8080"))

        val (upgradeResponse, closed) =
            outgoing
              .viaMat(webSocketFlow)(Keep.right)
              .toMat(incoming)(Keep.both)
              .run()

        val connected = upgradeResponse.flatMap { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
                Future.successful(Done)
            } else {
                throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
            }
        }

        connected.onComplete(println)
        closed.foreach(_ => println("closed"))
    }
}
