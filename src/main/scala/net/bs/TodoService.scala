package net.bs

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{ExceptionHandler, RequestContext}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import net.bs.models._
import spray.json._

import scala.concurrent.Future
import scala.util.control.NonFatal

object MyJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val TodoFormat = jsonFormat3(Todo)
}

trait TodoService {
  import MyJsonProtocol._

  import concurrent.duration._

  def repository: TodoRepository

  implicit def materializer: ActorMaterializer

  implicit val timeout = akka.util.Timeout(3.seconds)
  
  def logAndFail(ctx: RequestContext, e: Throwable)(implicit log: LoggingAdapter): HttpResponse = {
    log.error(e,"Request {} could not be handled normally", ctx.request)
    HttpResponse(status = StatusCodes.InternalServerError)
  }
  
  implicit def myExceptionHandler(implicit log: LoggingAdapter) = ExceptionHandler {
    case NonFatal(e) =>
      (ctx: RequestContext) => Future.successful(Complete(logAndFail(ctx,e)))
  }

  val showTodo = { id: String =>
     get {
       onSuccess(repository.show(id)) {
         case None =>
           complete(HttpResponse(status = NotFound))
         case Some(todo) =>
           completeWith(instanceOf[Todo]) {
             _ (todo)
           }
       }
     }
  }

  val updateTodo = { id: String =>
    put {
      entity(as[Todo]) { todo =>
        onSuccess(repository.update(todo)) { resp =>
          if (resp < 1) complete(HttpResponse(status = NotFound))
          else complete(HttpResponse(status = OK))
        }
      }
    }
  }

  val deleteTodo = { id: String =>
    delete {
      onSuccess(repository.delete(id)) { resp =>
          if (resp < 1)
            complete(HttpResponse(status = NotFound))
          else
            complete(HttpResponse(status = OK))
        }
    }
  }

  val listTodos = get {
    val source = Source.fromPublisher(repository.list())
    val todos = source.runFold(Seq.empty[Todo]){ case (agg,t) => agg :+ t }
    onSuccess(todos) { lst =>
      complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, lst.toJson.compactPrint)))
    }
  }

  val createTodo = post {
    entity(as[Todo]) { todo =>
        onSuccess(repository.insert(todo)) {
          case Left(id) =>
            redirect(s"/api/todo/$id",SeeOther)
          case Right(td) =>
            complete(Created, List(Location(s"/api/todo/${td.id.get}")), td)
        }
      }
    }


  def routes = {
    logRequestResult(("todo service req/resp",akka.event.Logging.DebugLevel)) {
      pathPrefix("api") {
        path("todo" / "\\w+".r) { id =>
          showTodo(id) ~
          updateTodo(id) ~
          deleteTodo(id)
        } ~
        path("todo") {
          listTodos ~ createTodo
        }
      } ~
      getFromResourceDirectory("public") ~
      pathEndOrSingleSlash {
        getFromResource("public/index.html")
      }
    }
  }
}