package io.github.insatiablebirdy

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.client.RequestBuilding

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.stream.scaladsl.Source

import scala.util.Success

import io.circe.parser._
import io.circe.generic.auto._


object InsatiableBirdy {
  def twitterEndpointFlow(endpoint: EndpointRequest)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) = {
    val connectionFlow = Http().cachedHostConnectionPoolHttps[Long]("stream.twitter.com")
    val request = endpoint.toAkkaWithCreds
    Source.single(request -> 1L)
      .via(connectionFlow)
      .map({
        case (Success(resp), _) =>
          resp.entity.dataBytes
        case asdf =>
          println(asdf)
          Source.empty
      })
      .flatMapConcat(a => a)
      .map(_.decodeString("UTF-8"))
      .scan("")((acc, curr) => if (acc.contains("\r\n")) curr else acc + curr)
      .map(json => decode[Tweet](json))
      .mapConcat({
        case Right(tweet) => List(tweet)
        case Left(_) => List()
      })
  }
}
