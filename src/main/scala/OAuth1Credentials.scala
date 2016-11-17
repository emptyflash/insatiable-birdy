package io.github.insatiablebirdy

import scala.language.postfixOps

import akka.http.scaladsl.model.headers.GenericHttpCredentials
import akka.http.scaladsl.model.HttpRequest
import akka.actor.ActorSystem

import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent._
import scala.concurrent.duration._

import java.net.URLEncoder


final case class OAuth1Credentials(
  consumerKey: String,
  consumerSecret: String,
  token: String,
  tokenSecret: String)(implicit system: ActorSystem) {
  import system.dispatcher

  private[insatiablebirdy] def toAkka(request: HttpRequest) = {
    val method = request.method.name
    val uri =
      request
        .uri
        .withScheme("https")
        .withHost("stream.twitter.com")
        .toString
    val consumer = new DefaultConsumerService(system.dispatcher)
    val koauthReq = KoauthRequest(method, uri, None, None)
    val oauthHeader = consumer
      .createOauthenticatedRequest(koauthReq, consumerKey, consumerSecret, token, tokenSecret)
      .map(_.header)
      .map(header => KoauthRequest.extractOauthParams(Some(header)))
      .map(_.toMap)
      .map({ map =>
        map ++ map.filterKeys(k => k == "oauth_signature").mapValues(v => URLEncoder.encode(v))
      })

    val header = Await.result(oauthHeader, 3 seconds)
    GenericHttpCredentials("OAuth", "", header)
  }
}
