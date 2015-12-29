package net.bs

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import net.bs.models.TodoRepository
import slick.jdbc.JdbcBackend

import scala.concurrent.duration._
import scala.concurrent.{Promise, Await, ExecutionContext}
import scala.io.StdIn

object Boot extends App with TodoService {

  val config = ConfigFactory.load()

  val host = config.getString("host")
    
  val port = config.getInt("port")
  
  implicit val system = ActorSystem("akka-http-todomvc")

  implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val db = JdbcBackend.Database.forConfig("tododb")
  val dbEc = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  // create repo
  lazy val repository = new TodoRepository(db)(dbEc)
  val schema = repository.createSchemaIfNotExists
  schema.onFailure {
    case x =>
      println("Failed to create schema against empty database")
      x.printStackTrace()
  }

  // create a new HttpServer using our handler and tell it where to bind to
  val handler = for {
    _ <- schema
    h <- Http().bindAndHandle(routes, interface = host, port = port)
  } yield h
  println(s"HTTP server bound to host $host and port $port.")

  sys.addShutdownHook({
    db.close()
    val fut = for {
      bound <- handler
      _ <- bound.unbind()
      terminated <- system.terminate()
    } yield terminated

    Await.result(fut, 3.seconds)
    ()
  })

  val neverFulfilled = Promise[Unit]().future
  Await.result(neverFulfilled, Duration.Inf)

}


