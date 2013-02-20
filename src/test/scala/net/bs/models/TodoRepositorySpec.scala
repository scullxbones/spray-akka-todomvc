package net.bs.models

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfter

import scala.slick.session.{Database, Session}
import scala.slick.driver.H2Driver

@RunWith(classOf[JUnitRunner])
class TodoRepositorySpec extends FunSpec with ShouldMatchers with BeforeAndAfter {
  
  val repository = new TodoRepository(H2Driver) 
  val database =       Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
  
  import repository._
  import repository.profile.simple._
  
  def run(closure: Session => Unit) = {
    database withSession { implicit session: Session =>
      repository.create
      closure(session)
    }
  }
  
	describe("A todo repository") {
	  
	  it("should be able to create itself on an empty database") {
	    run { implicit session: Session =>
	      val todo = create(new Todo(None, "test", false))
	      // No error thrown = success ?
	    }
	  }

	  it("should be able to insert a new Todo object and generate a key") {
	    run { implicit session: Session =>
	      val todo = create(new Todo(None, "test", false))
	      val retrieved = show(todo)
	      assert(retrieved.isDefined,retrieved)
	    }
	  }
	  
	  it("should return a Some[Todo] option for a key that already exists") {
	    run { implicit session: Session =>
	      val toSave = new Todo(None, "test", false)
	      val todo = create(toSave)
	      val retrieved = show(todo)
	      assert(Some(toSave) === retrieved)
	    }
	  }
	  
	  it("should return a None option for a key that does not exist") {
	    run { implicit session: Session =>
	      val toSave = new Todo(None, "test", false)
	      val todo = create(toSave)
	      val retrieved = show("does not exist")
	      assert(None === retrieved)
	    }
	  }
	  
	  it("should be able to list all todo objects in the database") {
	    run { implicit session: Session =>
	      val todos = List(new Todo(None, "test", false),new Todo(None, "test2", false),new Todo(None, "test3", false))
	      val ids = todos.map( t => create(t) )
	      val retrieved = list()
	      assert(3 === retrieved.size)
	    }
	  }
	  
	  it("should be able to create a new todo object") {
	    run { implicit session: Session =>
	      val todo = create(new Todo(None, "test", false))
	      val retrieved = show(todo)
	      assert(retrieved.isDefined,retrieved)
	    }
	  }
	  
	  it("should be able to update an existing todo object") {
	    run { implicit session: Session =>
	      val todo = create(new Todo(None, "test", false))
	      show(todo) match {
	        case Some(t) => {
	          val updated = new Todo(t.id, "updated title", true)
	          update(updated)
		      val retrieved = show(t.id.get)
		      assert(retrieved.isDefined,retrieved)
		      assert("updated title" === retrieved.get.title)
		      assert(true === retrieved.get.completed)
	        }
	        case None => throw new Exception("Couldn't find object")
	      }
	    }
	  }
	  
	  it("should be able to delete an existing todo object") {
	    run { implicit session: Session =>
	      val todo = create(new Todo(None, "test", false))
	      show(todo) match {
	        case Some(t) => {
	          delete(t.id.get)
	          assert(None === show(todo))
	        }
	        case None => throw new Exception("Couldn't find object")
	      }
	    }
	  }
	}
}