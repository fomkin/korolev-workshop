package com.github.korolevWorkshop.service.data

import java.util.UUID
import zhukov._
import zhukov.Default.auto._

final case class BlogPostComment(id: String, author: String, text: String, date: Long)

object BlogPostComment {
  implicit val marshaller = derivation.marshaller[BlogPostComment]
  implicit val unmarshaller = derivation.unmarshaller[BlogPostComment]
  implicit val sizeMeter = derivation.sizeMeter[BlogPostComment]
}