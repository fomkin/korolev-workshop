package com.github.korolevWorkshop.service

import com.github.korolevWorkshop.service.data.{BlogPost, BlogPostComment}
import org.iq80.leveldb.DB
import zhukov.{Marshaller, Unmarshaller}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class BlogPostService(db: DB)(implicit ec: ExecutionContext) {

  def getAllBlogPosts: Future[Seq[BlogPost]] = Future {
    val i = db.iterator()
    val buffer = mutable.Buffer.newBuilder[BlogPost]
    i.seek("post".getBytes)
    i.seekToFirst()
    while (i.hasNext) {
      val entry = i.next()
      val blogPost = Unmarshaller[BlogPost].read(entry.getValue)
      buffer += blogPost
    }
    buffer.result()
  }

  def getComments(blogPostId: String): Future[Seq[BlogPostComment]] = Future {
    val i = db.iterator()
    val buffer = mutable.Buffer.newBuilder[BlogPostComment]
    i.seek(s"comment$blogPostId".getBytes)
    i.seekToFirst()
    while (i.hasNext) {
      val entry = i.next()
      val blogPost = Unmarshaller[BlogPostComment].read(entry.getValue)
      buffer += blogPost
    }
    buffer
      .result()
      .sortBy(_.date)
  }

  def addBlogPost(blogPost: BlogPost): Future[Unit] = Future {
    db.put(s"post${blogPost.id}".getBytes, Marshaller[BlogPost].write(blogPost))
  }

  def addBlogPostComment(blogPostId: String, blogPostComment: BlogPostComment): Future[Unit] = Future {
    db.put(s"post$blogPostId".getBytes, Marshaller[BlogPostComment].write(blogPostComment))
  }
}
