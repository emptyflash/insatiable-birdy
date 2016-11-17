import org.scalatest._
import org.scalatest.Matchers._

import akka.http.scaladsl.client.RequestBuilding
import akka.actor.ActorSystem

import io.github.insatiablebirdy.OAuth1Credentials


class OAuth1CredentialsSpec extends FunSpec {
  implicit val system = ActorSystem("insatiable-birdy-test-oauth")
  describe("params") {
    it("should return a map with the correct keys") {
      val request = RequestBuilding.Post("/test")
      val creds = OAuth1Credentials(request, "test key", "test secret", "test token", "test secret")
      val result = creds.params - "oauth_nonce" - "oauth_signature" - "oauth_timestamp"
      val expected = Map(
        "oauth_signature_method" -> "HMAC-SHA1",
        "oauth_consumer_key" -> "test key",
        "oauth_version" -> "1.0",
        "oauth_token" -> "test token")
      result should be(expected)
    }
  }
}
