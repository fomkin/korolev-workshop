package com.github.korolevWorkshop.web

import java.io.File
import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import com.github.korolevWorkshop.service.BlogPostService
import com.github.korolevWorkshop.service.data.BlogPost
import korolev._
import korolev.akkahttp._
import korolev.execution._
import korolev.server._
import korolev.state.javaSerialization._
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory

import scala.concurrent.Future
import scala.util.Random

object KorolevWorkshop extends App {

  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val materializer: Materializer = ActorMaterializer()

  import org.iq80.leveldb.DB

  val options = new Options()
  options.createIfMissing(true)
  val db = Iq80DBFactory.factory.open(new File("db"), options)
  val blogPostService = new BlogPostService(db)

  val ctx = Context[Future, BlogState, Any]

  import ctx._
  import ctx.symbolDsl._

  private val config = KorolevServiceConfig[Future, BlogState, Any](
    router = Router.empty,
    stateStorage = StateStorage.default(BlogState()),
    render = { case _ =>
      'body("Hello world")
    }
  )

  private val route = akkaHttpService(config).apply(AkkaHttpServerConfig())

  Http().bindAndHandle(route, "0.0.0.0", 8081)
}
