package com.github.korolevWorkshop.web

import com.github.korolevWorkshop.service.data.{BlogPost, BlogPostComment}

sealed trait BlogEvent

object BlogEvent {
  final case class BlogPostAdded(blogPost: BlogPost) extends BlogEvent
  final case class BlogPostCommentAdded(blogPostComment: BlogPostComment) extends BlogEvent
}
