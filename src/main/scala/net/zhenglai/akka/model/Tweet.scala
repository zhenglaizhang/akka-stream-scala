package net.zhenglai.akka.model

final case class Author(handle: String)

final case class Hashtag(name: String)

object Hashtag {
  val akka = Hashtag("#akka")
}

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
}
