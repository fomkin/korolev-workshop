package com.github.korolevWorkshop.web

import java.io.File
import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import com.github.korolevWorkshop.service.BlogPostService
import com.github.korolevWorkshop.service.data.{BlogEvent, BlogPost}
import korolev._
import korolev.akkahttp._
import korolev.execution._
import korolev.server._
import korolev.state.EnvConfigurator
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

  val ctx = Context[Future, BlogState, BlogEvent]

  import ctx._
  import ctx.symbolDsl._

  val newBlogPostBody = elementId()
  val newBlogPostTitle = elementId()

  def onSubmit(access: Access) = {
    val res =
      for {
        _ <- access.transition { state =>
          state.copy(inProgress = true)
        }
        newTitle <- access.valueOf(newBlogPostTitle)
        newBody <- access.valueOf(newBlogPostBody)
        blogPost = BlogPost(
          id = Random.alphanumeric.take(10).mkString,
          title = newTitle,
          body = newBody,
          date = 0
        )
        _ <- blogPostService.addBlogPost(blogPost)
        _ <- access.transition { state =>
          state.copy(inProgress = false)
        }
      } yield ()

    res.recoverWith {
      case e =>
        access.transition(_.copy(error = Some(e.getMessage)))
    }
  }

  private val config = KorolevServiceConfig[Future, BlogState, BlogEvent](
    router = Router.empty,
    stateStorage = StateStorage.forDeviceId { _ =>
      blogPostService.getAllBlogPosts.map { blogPosts =>
        BlogState(blogPosts)
      }
    },
    render = { case state =>
      'body(
        'div(
          state.error map { error =>
            'span('backgroundColor @= "red", error)
          }
        ),
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
          event('submit)(onSubmit)
        )
      )
    },
    envConfigurator = EnvConfigurator { access =>
      val sink = Sink.foreachAsync[BlogEvent](1) {
        case BlogEvent.BlogPostAdded(blogPost) =>
          access.transition { state =>
            state.copy(blogPosts = state.blogPosts :+ blogPost)
          }
        case _ => Future.unit
      }
      blogPostService
        .topic
        .runWith(sink) // THIS IS WRONG

      Future.successful(
        EnvConfigurator.Env[Future, BlogEvent](
          onDestroy = () => Future.unit,
        )
      )
    }
  )

  private val route = akkaHttpService(config).apply(AkkaHttpServerConfig())

  Http().bindAndHandle(route, "0.0.0.0", 8081)
}
