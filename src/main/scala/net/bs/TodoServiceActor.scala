package net.bs

import spray.routing.HttpService
import spray.http._
import MediaTypes._
import akka.actor.Actor

class TodoServiceActor extends Actor with TodoService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(todoRoute)
}

trait TodoService extends HttpService {

  val todoRoute = {
    get {
      path("") {
        respondWithMediaType(`text/html`) { 
          complete("<html><body>Todo</body></html>")
        }
      }
    } ~
    getFromResourceDirectory("public")
  }
}