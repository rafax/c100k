package com.example

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import spray.can.server.Stats

import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")
  val statsActor = system.actorOf(Props(new StatsActor), "stats")
  implicit val ec = system.dispatcher

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http).tell(Http.Bind(service, interface = "0.0.0.0", port = 8080), statsActor)
}


class StatsActor extends Actor {
  override def receive: Receive = {
    case b: Http.Bound => {
      println(b)
      implicit val ec = context.dispatcher
      context.system.scheduler.schedule(Duration(1, TimeUnit.SECONDS), Duration(1, TimeUnit.SECONDS), sender(), Http.GetStats)
    }
    case s: Stats => println(s"Total: ${s.totalConnections} Open:${s.openConnections}")
  }
}
