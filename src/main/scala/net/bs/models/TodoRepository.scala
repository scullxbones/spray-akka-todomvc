package net.bs.models

import java.util.UUID

import slick.backend.DatabasePublisher
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import slick.jdbc.meta.MTable
import slick.lifted.{ProvenShape, Tag}

import scala.concurrent.{ExecutionContext, Future}

case class Todo(var id: Option[String] = None, title: String, completed: Boolean) {
  this.id = Some(id.getOrElse(UUID.randomUUID().toString.replaceAll("-","")))
}

class Todos(tag: Tag) extends Table[Todo](tag, "todos") {
  def id = column[String]("id", O.PrimaryKey)
  def title = column[String]("title")
  def completed = column[Boolean]("completed")

  def * : ProvenShape[Todo] =
    (id, title, completed) <> (mapRow, unMapRow)

  private def mapRow(tuple: (String,String,Boolean)) = tuple match {
    case (id, title, completed) => Todo(Option(id), title, completed)
  }

  private def unMapRow(todo: Todo): Option[(String,String,Boolean)] =
    todo.id.map(i => (i,todo.title,todo.completed))
}

class TodoRepository(database: JdbcBackend.DatabaseDef)(implicit ec: ExecutionContext) {

  val todos = TableQuery[Todos]
  
  def show(id: String): Future[Option[Todo]] = {
    val q = todos.filter(_.id === id).result.headOption
    database.run(q)
  }
  
  def update(todo: Todo): Future[Int] = {
    val q = (for {
      t <- todos if t.id === todo.id.get
    } yield (t.title, t.completed)).update(todo.title,todo.completed)
    database.run(q.transactionally)
  }
  
  def delete(id: String): Future[Int] = {
    val q = todos.filter(_.id === id).delete
    database.run(q.transactionally)
  }
  
  def list(): DatabasePublisher[Todo] = {
    val q = (for {
      t <- todos
    } yield t).result
    database.stream(q)
  }
  
  def insert(todo: Todo): Future[Either[String, Todo]] = {
    val q = todos.filter(_.id === todo.id.get).result.headOption.flatMap {
      case Some(td) =>
        DBIO.successful(Left(td.id.get))
      case None =>
        (todos += todo).andThen(
          DBIO.successful(Right(todo))
        )
    }.transactionally
    database.run(q)
  }

  def createSchemaIfNotExists: Future[Unit] = {
    for {
      tables <- database.run(MTable.getTables)
      _ <- {
        if (tables.exists(_.name.name == "todos")) Future.successful(())
        else database.run(todos.schema.create)
      }
    } yield ()
  }
}