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
import reflect.ClassTag
import spray.http.HttpHeaders._
import spray.http.StatusCodes._
import spray.util.LoggingContext
import scala.util.Failure

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
  
  def logAndFail(ctx: RequestContext, e: Throwable)(implicit log: LoggingContext) {
    log.error(e,"Request {} could not be handled normally", ctx.request)
    ctx.complete(InternalServerError)
  }
  
  implicit def myExceptionHandler(implicit log: LoggingContext) =
	  ExceptionHandler.fromPF {
	    case e: Exception => ctx =>
	      logAndFail(ctx,e)
	  }
  
  val todoRoute = {
    logRequestResponse(("todo service req/resp",akka.event.Logging.DebugLevel)) {
    pathPrefix("api") {
    	path("todo" / "\\w+".r) { id =>
    	  get { ctx =>
    	    ask(repoActor, ShowMessage(id))
    	    	.mapTo[ShowResponse]
		        .onComplete {
		            case Success(resp) => 
		              resp.todo match { 
		                case None => ctx.complete(NotFound)
		                case Some(todo) => ctx.complete(todo)
					  }
		            case Failure(e) =>
		              logAndFail(ctx,e)
    	    	}
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
				            case Failure(e) =>
				              logAndFail(ctx,e)
		    	      	}
	    	      }
    	      }
    	  } ~
    	  delete { 
    		ctx =>
    	      ask(repoActor, DeleteMessage(id))
    	      	.mapTo[UpdateResponse]
    	        .onComplete {
	    	        case Success(resp) => 
	    	          if(resp.response == 0) ctx.complete(NotFound)
	    	          else ctx.complete(OK)
		            case Failure(e) =>
		              logAndFail(ctx,e)
    	      	}
    	  }
    	} ~
	    path("todo") {
	      get {
	        ctx =>
	          ask(repoActor, ListMessage())
	          	.mapTo[ListResponse]
	          	.onComplete {
		            case Success(resp) => ctx.complete(resp.todos)
		            case Failure(e) =>
		              logAndFail(ctx,e)
	            }
	      } ~
	      post {
    	      entity(as[Todo]) { todo => 
    	        ctx => {
		    	      ask(repoActor, CreateMessage(todo))
		    	      	.mapTo[CreateResponse]
		    	        .onComplete {
			    	        case Success(resp) => 
		    	        	  ctx.complete(Created, List(Location("%s/%s".format(ctx.request.uri,resp.todo.id.get))), resp.todo)
				            case Failure(e) =>
				              logAndFail(ctx,e)
		    	      	}
	    	      }
    	      }
	      }
	    }
    } ~
    getFromResourceDirectory("public")
    }
  }
}