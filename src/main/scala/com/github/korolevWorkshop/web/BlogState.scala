package com.github.korolevWorkshop.web

import com.github.korolevWorkshop.service.data.BlogPost

case class BlogState(
  blogPosts: Seq[BlogPost],
  inProgress: Boolean = false,
  error: Option[String] = None
)


