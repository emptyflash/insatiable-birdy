package io.github.insatiablebirdy

import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.client.RequestBuilding

import akka.actor.ActorSystem
import akka.stream._

import akka.stream.scaladsl._

import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent._
import scala.concurrent.duration._
import java.net.URLEncoder

import scala.util.{Failure, Success}

import io.circe.syntax._
import io.circe._
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
