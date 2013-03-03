package net.bs.models

import scala.slick.driver.{ExtendedProfile,H2Driver,PostgresDriver}
import java.util.UUID

trait Profile {
  val profile: ExtendedProfile
}

sealed trait Domain

case class Todo(var id: Option[String] = None, title: String, completed: Boolean) extends Domain {
  this.id = Some(id.getOrElse(UUID.randomUUID().toString().replaceAll("-","")))
}

trait TodoComponent { this: Profile =>
  import profile.simple._
  
	object Todos extends Table[Todo]("todos") {
	  def id = column[String]("id", O.PrimaryKey)
	  def title = column[String]("title")
	  def completed = column[Boolean]("completed")
	  def * = id.? ~ title ~ completed <> (Todo, Todo.unapply _)
	}
  
  def show(id: String)(implicit session: Session): Option[Todo] = {
    val q = Query(Todos).filter(_.id === id)
    q.firstOption()
  }
  
  def update(todo: Todo)(implicit session: Session) = {
    val q = for {
      t <- Todos if t.id === todo.id.get
    } yield (t.title ~ t.completed)
    q.update(todo.title,todo.completed)
  }
  
  def delete(id: String)(implicit session: Session) = {
    val q = Query(Todos).filter(_.id === id)
    q.delete
  }
  
  def list()(implicit session: Session): List[Todo] = {
    val q = Query(Todos)
    q.list()
  }
  
  def create(todo: Todo)(implicit session: Session): Todo = {
    Todos.insert(todo)
    todo
  } 
  
}


class TodoRepository(override val profile: ExtendedProfile) extends TodoComponent with Profile {
  import profile.simple._
  
  def create(implicit session: Session) = {
    Todos.ddl.create
  }
}
