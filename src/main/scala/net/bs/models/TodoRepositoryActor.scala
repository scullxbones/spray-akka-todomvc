package net.bs.models

import scala.slick.session.Session
import akka.actor.Actor
import akka.actor.actorRef2Scala
import scala.slick.session.Database

sealed trait QueryMessage
case class ShowMessage(id: String) extends QueryMessage
case class ListMessage() extends QueryMessage
case class ShowResponse(todo: Either[java.lang.Throwable, Option[Todo]]) extends QueryMessage
case class ListResponse(todos: Either[java.lang.Throwable, List[Todo]]) extends QueryMessage

sealed trait CommandMessage
case class UpdateMessage(todo: Todo) extends CommandMessage
case class CreateMessage(todo: Todo) extends CommandMessage
case class DeleteMessage(id: String) extends CommandMessage
case class UpdateResponse(response: Either[java.lang.Throwable, Int]) extends CommandMessage
case class CreateResponse(response: Either[java.lang.Throwable, String]) extends CommandMessage

class TodoRepositoryActor(repo: TodoComponent)(implicit database: Database) extends Actor {

  import repo.Todos._
  
  def receive = {
    case s: ShowMessage => withSession(receiveShow)(s)
    
    case l: ListMessage => withSession(receiveList)(l)
    
    case u: UpdateMessage => withSession(receiveUpdate)(u)
    
    case c: CreateMessage => withSession(receiveCreate)(c)
    
    case d: DeleteMessage => withSession(receiveDelete)(d)
    
    case x => unhandled(x)
  }
  
  private def withSession[T](receiver: (T,Session) => Unit)( message: T)(implicit database: Database): Unit = {
    database withSession { session: Session =>
      receiver(message, session)
    }
  }
  
  private def receiveShow(s: ShowMessage, session: Session): Unit = {
    try {
    	 val result = repo.show(s.id)(session)
       sender ! ShowResponse(Right(result))
    }
    catch {
      case ex:Throwable => sender ! ShowResponse(Left(ex))
    }
  }
  
  private def receiveList(l: ListMessage, session: Session): Unit = {
    try {
	      val result = repo.list()(session)
	      sender ! ListResponse(Right(result))
    }
    catch {
      case ex:Throwable => sender ! ListResponse(Left(ex))
    }
  }
  
  private def receiveUpdate(u: UpdateMessage, session: Session): Unit = {
    try {
	      val result = repo.update(u.todo)(session)
	      sender ! UpdateResponse(Right(result))
    }
    catch {
      case ex:Throwable => sender ! UpdateResponse(Left(ex))
    }
  }
  
  private def receiveCreate(c: CreateMessage, session: Session): Unit = {
    try {
	      val result = repo.create(c.todo)(session)
	      sender ! CreateResponse(Right(result))
    }
    catch {
      case ex:Throwable => sender ! CreateResponse(Left(ex))
    }
  }
  
  private def receiveDelete(d: DeleteMessage, session: Session): Unit = {
    try {
    	  val result = repo.delete(d.id)(session)
    	  sender ! UpdateResponse(Right(result))
    }
    catch {
      case ex: Throwable => sender ! UpdateResponse(Left(ex))
    }
  }
}