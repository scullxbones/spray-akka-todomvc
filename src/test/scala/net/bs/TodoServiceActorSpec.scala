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
		        case UpdateMessage(_) => sender ! new UpdateResponse(1)
		      }
		    }
		))
	  
		val doesNotExist = system.actorOf(Props(
		    new Actor {
		      def receive = {
		        case _:ShowMessage => sender ! new ShowResponse(None) 
		        case _:UpdateMessage => sender ! new UpdateResponse(0) 
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
  
  describe("the service") {
    it("show should return a todo if it exists") {
	  _repoActor = Mocks.exists
	  
      Get("/api/todo/TEST") ~> route ~> check {
        val todo = entityAs[String]
        todo should include ("title")
        todo should include ("TEST")
      }
    }
    it("show should return a 404 if it does not exist") {
	  _repoActor = Mocks.doesNotExist
	  
      Get("/api/todo/TEST") ~> route ~> check {
        status.value should equal(404)
      }
    }
    it("show should return a 500 if the future errors out") {
	  _repoActor = Mocks.fails
	  
      Get("/api/todo/TEST") ~> route ~> check {
        status.value should equal(500)
      }
    }
    it("update should return a 200 on successful update of an existing todo") {
      _repoActor = Mocks.exists
      
      val todo = Todo(Some("TEST"),"new title",true)
      Put("/api/todo/TEST",todo) ~> route ~> check {
        status.value should equal(200)
      }
    }
    it("update should return a 404 on an attempt to update a todo that doesn't exist") {
      _repoActor = Mocks.doesNotExist
      
      val todo = Todo(Some("TEST"),"new title",true)
      Put("/api/todo/TEST",todo) ~> route ~> check {
        status.value should equal(404)
      }
    }
    it("update should return a 500 if the future errors out") {
      _repoActor = Mocks.fails
      
      val todo = Todo(Some("TEST"),"new title",true)
      Put("/api/todo/TEST",todo) ~> route ~> check {
        status.value should equal(500)
      }
    }
  }

}