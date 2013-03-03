package net.bs.models

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestActorRef
import org.scalatest.mock.MockitoSugar
import scala.slick.session.Database
import akka.actor.Props
import akka.actor.ActorSystem
import org.mockito.Mockito._
import scala.slick.session.Session
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class TodoRepositoryActorSpec extends FunSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter {
  
  implicit val timeout = Timeout(10)
  implicit val actors = ActorSystem("test")
  val repoMock = mock[TodoComponent]
  implicit val dbMock = mock[Database]
  implicit val sessMock = mock[Session]
  val actorRef = TestActorRef(Props(new TodoRepositoryActor(repoMock)))
  
  after {
    actors.shutdown
  }

  describe("A Todo Repository Actor") {
    it ("Should respond to a show request with Some or None depending on whether Todo exists") {
      val one = Todo(Some("1"),"title",true)
      when(repoMock.show("1")).thenReturn(Some(one))
      when(repoMock.show("2")).thenReturn(None)
      
      val actualOne = actorRef ? ShowMessage("1")
      
      actualOne.onSuccess(_ match {
        case m: ShowResponse => {
         m.todo match {
           case Some(todo) => Some("1") should equal (todo.id)
           case None => fail("Expected to find todo")
         }  
        }
      })
      
      val actualTwo = actorRef ? ShowMessage("2")
      actualTwo.onSuccess(_ match {
        case m: ShowResponse => {
          m.todo match {
            case Some(_) =>fail("Expected not to find todo")
            case None => // Success
          }
        }
      })
    }
    
    it ("Should respond to a list request with a list of the existing Todos") {
      val todos = List(Todo(Some("1"),"title",true),Todo(Some("2"),"title2",true),Todo(Some("3"),"title3",true))
      when(repoMock.list()).thenReturn(todos)
      
      val actualOne = actorRef ? ListMessage()
      actualOne.onSuccess(_ match {
        case m: ListResponse => m.todos should equal(todos)
      })
    }
    
    it ("Should respond to a create request with the created key") {
    	val todo = Todo(None, "title", true)
    	when(repoMock.create(todo)).thenReturn(todo.copy(id=Some("one")))
    	
    	val actual = actorRef ? CreateMessage(todo)
    	actual.onSuccess(_ match {
    	  case m:CreateResponse => m.todo match {
    	    case todo:Todo => "one" should equal(todo.id.get)
    	    case _ => fail("Expected success")
    	  }
    	})
    }
    
    it ("Should respond to a update request with a count of how many rows were updated") {
    	val todo = Todo(None, "title", true)
    	when(repoMock.update(todo)).thenReturn(1)
    	
    	val actual = actorRef ? UpdateMessage(todo)
    	actual.onSuccess(_ match {
    	  case m:UpdateResponse => m.response match {
    	    case cnt:Int => 1 should equal(cnt)
    	  }
    	})
    }
    
    it ("Should respond to a delete request with a count of how many rows were deleted") {
    	val todo = Todo(None, "title", true)
    	when(repoMock.delete("one")).thenReturn(11)
    	
    	val actual = actorRef ? DeleteMessage("one")
    	actual.onSuccess(_ match {
    	  case m:UpdateResponse => m.response match {
    	    case cnt:Int => 11 should equal(cnt)
    	  }
    	})
    }
  }
}