package net.bs

import akka.actor.Props
import spray.can.server.SprayCanHttpServerApp

object Boot extends App with SprayCanHttpServerApp {
  
  val host = "0.0.0.0"
    
  val port = Option(System.getenv("http.port")).getOrElse("8080").toInt
  
//  Option(System.getenv("DATABASE_URL")).

  // create and start our service actor
  val service = system.actorOf(Props[TodoServiceActor], "todo-service")

  // create a new HttpServer using our handler and tell it where to bind to
  newHttpServer(service) ! Bind(interface = host, port = port)
}


