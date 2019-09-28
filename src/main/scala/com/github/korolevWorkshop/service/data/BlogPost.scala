package com.github.korolevWorkshop.service.data

import java.util.UUID
import zhukov._
import zhukov.Default.auto._

final case class BlogPost(id: String, title: String, body: String, date: Long)

object BlogPost {
  implicit val marshaller = derivation.marshaller[BlogPost]
  implicit val unmarshaller = derivation.unmarshaller[BlogPost]
  implicit val sizeMeter = derivation.sizeMeter[BlogPost]
}