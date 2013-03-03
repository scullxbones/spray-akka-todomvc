package net.bs

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import spray.testkit.ScalatestRouteTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestActorRef
import akka.actor.Props
import akka.testkit.TestProbe
import scala.concurrent.duration._
import akka.actor._
import scala.concurrent.Future
import net.bs.models._
import akka.testkit.TestProbe
import akka.util.Timeout
import akka.testkit.TestActor
import MyJsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.http.HttpHeaders.Location

@RunWith(classOf[JUnitRunner])
class TodoServiceActorSpec extends FunSpec with ShouldMatchers with ScalatestRouteTest with TodoService {
  def actorRefFactory = system // connect the DSL to the test ActorSystem
  val route = todoRoute 
  implicit val _system = system
  var _repoActor: ActorRef = Mocks.exists
  def repoActor = _repoActor
  
	object Mocks { 
		implicit val askTimeout: Timeout = 3 second span 
	
		val exists = system.actorOf(Props(
		    new Actor {
		      def receive = {
		        case ShowMessage(id) => sender ! new ShowResponse(Some(Todo(Some(id),"title",false))) 
		        case UpdateMessage(_) | DeleteMessage(_) => sender ! new UpdateResponse(1)
		        case _:ListMessage => sender ! new ListResponse(List(Todo(Some("TEST"),"title2",false)))
		      }
		    }
		))
	  
		val doesNotExist = system.actorOf(Props(
		    new Actor {
		      def receive = {
		        case _:ShowMessage => sender ! new ShowResponse(None) 
		        case _:UpdateMessage | _:DeleteMessage => sender ! new UpdateResponse(0)
		        case _:ListMessage => sender ! new ListResponse(List.empty)
		        case CreateMessage(todo) => sender ! new CreateResponse(todo.copy(id = Some("TEST")))
		      }
		    }
		))
		
		val fails = system.actorOf(Props(
		    new Actor {
		      def receive = {
		        case _ => sender ! akka.actor.Status.Failure(new Exception) 
		      }
		    }
		))
  }
  
  describe("the service show") {
    it("should return a todo if it exists") {
	  _repoActor = Mocks.exists
	  
      Get("/api/todo/TEST") ~> route ~> check {
        val todo = entityAs[String]
        todo should include ("title")
        todo should include ("TEST")
      }
    }
    it("should return a 404 if it does not exist") {
	  _repoActor = Mocks.doesNotExist
	  
      Get("/api/todo/TEST") ~> route ~> check {
        status.value should equal(404)
      }
    }
    it("should return a 500 if the future errors out") {
	  _repoActor = Mocks.fails
	  
      Get("/api/todo/TEST") ~> route ~> check {
        status.value should equal(500)
      }
    }
  }
  
  describe("the service update") {
    val todo = Todo(Some("TEST"),"new title",true)
    it("should return a 200 on successful update of an existing todo") {
      _repoActor = Mocks.exists
      
      Put("/api/todo/TEST",todo) ~> route ~> check {
        status.value should equal(200)
      }
    }
    it("should return a 404 on an attempt to update a todo that doesn't exist") {
      _repoActor = Mocks.doesNotExist
      
      Put("/api/todo/TEST",todo) ~> route ~> check {
        status.value should equal(404)
      }
    }
    it("should return a 500 if the future errors out") {
      _repoActor = Mocks.fails
      
      Put("/api/todo/TEST",todo) ~> route ~> check {
        status.value should equal(500)
      }
    }
  }

  describe("the service delete") {
    it("should return a 200 on successful delete of an existing todo") {
      _repoActor = Mocks.exists
      
      Delete("/api/todo/TEST") ~> route ~> check {
        status.value should equal(200)
      }
    }
    it("should return a 404 on an attempt to delete a todo that doesn't exist") {
      _repoActor = Mocks.doesNotExist
      
      Delete("/api/todo/TEST") ~> route ~> check {
        status.value should equal(404)
      }
    }
    it("should return a 500 if the future errors out") {
      _repoActor = Mocks.fails
      
      Delete("/api/todo/TEST") ~> route ~> check {
        status.value should equal(500)
      }
    }
  }
  
  describe("the service list") {
    it("should return a populated list of every todo when some exist") {
      _repoActor = Mocks.exists
      
      Get("/api/todo") ~> route ~> check {
        status.value should equal(200)
        val todoList = entityAs[List[Todo]]
        todoList should have size 1
        val todo = todoList(0)
        todo.title should be ("title2")
        todo.id should be (Some("TEST"))
      }
    }

    it("should return an empty list with no todos") {
      _repoActor = Mocks.doesNotExist
      
      Get("/api/todo") ~> route ~> check {
        status.value should equal(200)
        val todoList = entityAs[List[Todo]]
        todoList should have size 0
      }
    }

    it("should return a 500 if the future errors out") {
      _repoActor = Mocks.fails
      
      Get("/api/todo") ~> route ~> check {
        status.value should equal(500)
      }
    }

  }
  
  describe("the service create") {
    val todo = Todo(None,"new title",true)
    
    it("when the todo does exist, should perform a 303 see other redirect") {
      _repoActor = Mocks.exists
      
      Post("/api/todo",todo) ~> route ~> check {
        status.value should be (303)
        response.header[Location] should be (Some(Location("/api/todo/TEST")))
      }
      
      
    }
    
    it("when the todo does not exist, should present the created entity, a 201 created status, and a location redirect") {
      _repoActor = Mocks.doesNotExist
      
      Post("/api/todo",todo) ~> route ~> check {
        status.value should be (201)
        response.header[Location] should be (Some(Location("/api/todo/TEST")))
        val withId = entityAs[Todo]
        withId.title should be ("new title") 
        withId.id should be (Some("TEST"))
      }
    }
    
  }

}