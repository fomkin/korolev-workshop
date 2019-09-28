package com.github.korolevWorkshop.service.data

sealed trait BlogEvent

object BlogEvent {
  final case class BlogPostAdded(blogPost: BlogPost) extends BlogEvent
  final case class BlogPostCommentAdded(blogPostComment: BlogPostComment) extends BlogEvent
}
