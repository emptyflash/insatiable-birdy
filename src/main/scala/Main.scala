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

  def run = {
    implicit val system = ActorSystem("insatiable-birdy")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher
    val consumerKey = "BIkDuViEPFW6aB5BaSuDEJQAh"
    val consumerSecret = "PIo6VPZpvmsyJi0y1H5ahT9ufy2a4YF3oQi34Ce2ypBM7nx5vA"
    val accessToken = "29611194-0jKqozgP37W9jov15aMAbDmV2ozBlchH2whJI3ERI"
    val tokenSecret = "NnYjyg9RVWI9AowhVcp5eWXUdFV12n3QcxvMyYkAy8Pl2"
    val credentials = OAuth1Credentials(
      consumerKey,
      consumerSecret,
      accessToken,
      tokenSecret)
    val request = FilterEndpoint(credentials, Track(List("#np")))
    twitterEndpointFlow(request).runForeach(println)
  }
}
