package net.bs

import spray.routing.HttpService
import spray.http._
import StatusCodes._
import MediaTypes._
import akka.actor.Actor
import spray.routing.directives._
import spray.routing._
import Directives._
import akka.actor.ActorRef
import net.bs.models.ShowMessage
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import net.bs.models.ShowResponse
import scala.util.Failure
import scala.util.Success
import net.bs.models._
import spray.httpx.marshalling._
import spray.json.DefaultJsonProtocol
import akka.event.Logging
import akka.actor.ActorLogging
import akka.event.Logging._

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val TodoFormat = jsonFormat3(Todo)
}

import MyJsonProtocol._
import spray.httpx.SprayJsonSupport._


class TodoServiceActor(_repoActor: ActorRef) extends Actor with TodoService with ActorLogging {

  def repoActor = _repoActor
  
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = 
      runRoute(todoRoute)

}

trait TodoService extends HttpService {

  def repoActor: ActorRef
  implicit val timeout = Timeout(3000)
  
  val todoRoute = {
    logRequestResponse(("todo service req/resp",akka.event.Logging.DebugLevel)) {
    pathPrefix("api") {
    	path("todo" / "\\w+".r) { id =>
    	  get { ctx =>
    	      val show = new ShowMessage(id)
		      val result = (repoActor ? show).mapTo[ShowResponse]
		      result.onSuccess(_.todo match { 
                case None => ctx.complete(NotFound)
                case Some(todo) => ctx.complete(todo)
			  })
			  result.onFailure(_ match {
			    case _ => ctx.complete(InternalServerError)
			  })
    	  } ~
    	  put {
    	      entity(as[Todo]) { todo => 
    	        ctx => {
		    	      ask(repoActor, UpdateMessage(todo))
		    	      	.mapTo[UpdateResponse]
		    	        .onComplete {
			    	        case Success(resp) => 
			    	          if(resp.response == 0) ctx.complete(NotFound)
			    	          else ctx.complete(OK)
			    	        case Failure(_) =>
			    	          ctx.complete(InternalServerError)
		    	      	}
	    	      }
    	      }
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
}