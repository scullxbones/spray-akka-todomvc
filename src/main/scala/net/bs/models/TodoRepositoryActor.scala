package net.bs.models

import scala.slick.session.Session
import akka.actor.Actor
import akka.actor.actorRef2Scala
import scala.slick.session.Database
import scala.util.Try
import akka.actor.ActorLogging

sealed trait QueryMessage
case class ShowMessage(id: String) extends QueryMessage
case class ListMessage() extends QueryMessage
case class ShowResponse(todo: Option[Todo]) extends QueryMessage
case class ListResponse(todos: List[Todo]) extends QueryMessage

sealed trait CommandMessage
case class UpdateMessage(todo: Todo) extends CommandMessage
case class CreateMessage(todo: Todo) extends CommandMessage
case class DeleteMessage(id: String) extends CommandMessage
case class UpdateResponse(response: Int) extends CommandMessage
case class CreateResponse(todo: Todo) extends CommandMessage

class TodoRepositoryActor(repo: TodoComponent)(implicit database: Database) extends Actor with ActorLogging {

  import repo.Todos._

  def receive = {
    case s: ShowMessage => withSession(receiveShow)(s)

    case l: ListMessage => withSession(receiveList)(l)

    case u: UpdateMessage => withSession(receiveUpdate)(u)

    case c: CreateMessage => withSession(receiveCreate)(c)

    case d: DeleteMessage => withSession(receiveDelete)(d)

    case x => unhandled(x)
  }

  private def withSession[T](receiver: (T, Session) => Unit)(message: T)(implicit database: Database): Unit = {
    database withSession { session: Session =>
      receiver(message, session)
    }
  }

  private def receiveShow(s: ShowMessage, session: Session): Unit = {
    try {
      log.debug("Received show request for id {}",s.id)
	    val result = repo.show(s.id)(session)
	    sender ! ShowResponse(result)
	} 
    catch {
	  case e: Exception ⇒
      	log.error(e,"Error while processing show request")
	    sender ! akka.actor.Status.Failure(e)
	    throw e
	}
  }

  private def receiveList(l: ListMessage, session: Session): Unit = {
    try {
      val result = repo.list()(session)
      sender ! ListResponse(result)
	} 
    catch {
	  case e: Exception ⇒
	    sender ! akka.actor.Status.Failure(e)
	    throw e
	}
  }

  private def receiveUpdate(u: UpdateMessage, session: Session): Unit = {
    try {
      val result = repo.update(u.todo)(session)
      sender ! UpdateResponse(result)
	} 
    catch {
	  case e: Exception ⇒
	    sender ! akka.actor.Status.Failure(e)
	    throw e
	}
  }

  private def receiveCreate(c: CreateMessage, session: Session): Unit = {
    try {
      val result = repo.create(c.todo)(session)
      sender ! CreateResponse(result)
	} 
    catch {
	  case e: Exception ⇒
	    sender ! akka.actor.Status.Failure(e)
	    throw e
	}
  }

  private def receiveDelete(d: DeleteMessage, session: Session): Unit = {
    try {
      val result = repo.delete(d.id)(session)
      sender ! UpdateResponse(result)
	} 
    catch {
	  case e: Exception ⇒
	    sender ! akka.actor.Status.Failure(e)
	    throw e
	}
  }
}