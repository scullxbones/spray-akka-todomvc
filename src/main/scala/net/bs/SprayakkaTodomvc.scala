package net.bs

import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import akka.event.Logging._
import spray.routing.SimpleRoutingApp
import spray.routing._
import Directives._
import spray.routing.directives.LoggingMagnet._

object SprayAkkaTodoMVC extends App with SimpleRoutingApp {
  startServer(interface = "localhost", port = 8080) {
    path("hello") {
      get {
        complete {
          <h1>Say hello to spray</h1>
        }
      }
    } ~ 
    get {
      logRequestResponse(("public",DebugLevel)) {
    	  getFromResourceDirectory("public") //."+_.request.path.replaceAllLiterally("/","."))
      }

    }
  }
  
}
