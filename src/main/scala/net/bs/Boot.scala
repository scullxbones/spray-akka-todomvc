package net.bs

import akka.actor.Props
import spray.can.server.SprayCanHttpServerApp
import scala.slick.session.Database
import java.net.URI
import net.bs.models.TodoRepository
import net.bs.models.TodoRepositoryActor
import scala.slick.driver.ExtendedProfile
import scala.slick.driver.PostgresDriver
import scala.slick.driver.H2Driver
import scala.slick.session.Session

object Boot extends App with SprayCanHttpServerApp {
  
  val host = "0.0.0.0"
    
  val port = Option(System.getenv("PORT")).getOrElse("8080").toInt
  
  implicit val (db :Database,driver: ExtendedProfile) = Option(System.getenv("DATABASE_URL")) match {
    case Some(url) => {
	    val dbUri = new URI(url)
	
	    val dbUrl = dbUri.getUserInfo().split(":") match {
	      case Array(user,pass) => {
	        val urlStr = "jdbc:postgresql://%s:%s/%s".format(dbUri.getHost(),dbUri.getPort(),dbUri.getPath())
	        (Database.forURL(urlStr, driver = "org.postgresql.Driver", user = user, password = pass), PostgresDriver)
	      }
	      case _ => throw new IllegalArgumentException("Unrecognized url (too many colons): "+url)
	    }
    }
    case None => {
      (Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver"), H2Driver)
    }
  }

  // create repo
  val repo = new TodoRepository(driver)
  db.withSession { implicit session: Session =>
  	repo.create
  }
  
  // create child repo actor
  val repoActor = system.actorOf(Props(new TodoRepositoryActor(repo)),"todo-repository")
  
  // create and start our service actor
  val service = system.actorOf(Props(new TodoServiceActor(repoActor)), "todo-service")
  
  // create a new HttpServer using our handler and tell it where to bind to
  newHttpServer(service) ! Bind(interface = host, port = port)
}


