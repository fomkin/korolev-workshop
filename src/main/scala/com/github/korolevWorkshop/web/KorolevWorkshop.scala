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

  val options = new Options()
  options.createIfMissing(true)
  val db = Iq80DBFactory.factory.open(new File("db"), options)
  val blogPostService = new BlogPostService(db)

  val ctx = Context[Future, BlogState, Any]

  import ctx._
  import ctx.symbolDsl._

  val newBlogPostBody = elementId()
  val newBlogPostTitle = elementId()

  private val config = KorolevServiceConfig[Future, BlogState, Any](
    router = Router.empty,
    stateStorage = StateStorage.default(BlogState(Nil)),
    render = { case state =>
      'body(
        'div(
          state.blogPosts map { blogPost =>
            'div(
              'h3(blogPost.title),
              'p(blogPost.body)
            )
          }
        ),
        'form(
          'input(newBlogPostTitle, 'type /= "text", 'placeholder /= "Title"),
          'input(newBlogPostBody, 'type /= "text", 'placeholder /= "Body"),
          'button("Submit"),
          event('submit) { access =>
            for {
              newTitle <- access.valueOf(newBlogPostTitle)
              newBody <- access.valueOf(newBlogPostBody)
              blogPost = BlogPost(
                id = Random.alphanumeric.take(10).mkString,
                title = newTitle,
                body = newBody,
                date = 0
              )
              _ <- access.transition { state =>
                state.copy(blogPosts = state.blogPosts :+ blogPost)
              }
            } yield ()
          }
        )
      )
    }
  )

  private val route = akkaHttpService(config).apply(AkkaHttpServerConfig())

  Http().bindAndHandle(route, "0.0.0.0", 8081)
}
