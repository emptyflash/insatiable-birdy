package io.github.insatiablebirdy

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.Authorization


sealed trait EndpointRequest {
  private[insatiablebirdy] def toAkka: HttpRequest
  def requestPath: String
  def credentials: OAuth1Credentials

  lazy val requestBase = "/1.1/statuses"
  lazy val toAkkaWithCreds = {
    val request = toAkka
    val creds = credentials.toAkka(request)
    request.withHeaders(Authorization(creds))
  }
}

final case class FilterEndpoint(
  credentials: OAuth1Credentials,
  track: Track = Track(List()),
  follow: Follow = Follow(List()),
  locations: Locations = Locations(List())
) extends EndpointRequest {

  lazy val requestPath = requestBase + "/filter.json"
  private[insatiablebirdy] lazy val toAkka = {
    val params = Map(
      "track" -> track.toString,
      "follow" -> follow.toString,
      "locations" -> locations.toString
    ).filter(_._2.nonEmpty)
    val uri = Uri(requestPath).withQuery(Uri.Query(params))

    RequestBuilding.Post(uri)
  }
}

final case class SampleEndpoint(credentials: OAuth1Credentials) extends EndpointRequest {
  lazy val requestPath = requestBase + "/sample.json"
  lazy val toAkka =
    RequestBuilding.Post(requestPath)
}
final case class FirehostEndpoint(credentials: OAuth1Credentials) extends EndpointRequest {
  lazy val requestPath = requestBase + "/firehose.json"
  lazy val toAkka = RequestBuilding.Post(requestPath)
}


final case class UserId(id: String)
final case class Location(lat: Double, long: Double) {
  override def toString = lat + "," + long
}

final case class Follow(userIds: Seq[UserId]) {
  override def toString = userIds.map(_.id).mkString(",")
}
final case class Track(terms: Seq[String]) {
  override def toString = terms.mkString(",")
}
final case class Locations(locations: Seq[Location]) {
  override def toString = locations.map(_.toString).mkString(",")
}
