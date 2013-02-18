package net.bs

import spray.routing.HttpService
import spray.http._
import MediaTypes._
import akka.actor.Actor
import spray.routing._
import spray.routing.directives._
import Directives._

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
    pathPrefix("api") {
    	path("todo" / "\\w+".r) { id =>
    	  get {
    	    complete("SHOW (GET) received for id = "+id)
    	  } ~
    	  put {
    	    complete("UPDATE (PUT) received for id = "+id)
    	  } ~
    	  delete {
    	    complete("DELETE (DELETE) received for id = "+id)
    	  }
    	} ~
	    path("todo") {
	      get {
	          complete("LIST (GET) received")
	      } ~
	      post {
	        complete ("CREATE (POST) received")
	      }
	    }
    } ~
    getFromResourceDirectory("public")
  }
}