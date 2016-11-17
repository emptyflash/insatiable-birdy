# Insatiable Birdy
A twitter streaming client using akka-http

## Example

``` scala
import io.github.insatiablebirdy._

implicit val system = ActorSystem("insatiable-birdy")
implicit val materializer = ActorMaterializer()
import system.dispatcher
val consumerKey = "<your consumer key>"
val consumerSecret = "<your consumer secret>"
val accessToken = "<your access token>"
val tokenSecret = "<your token secret>"
val credentials = OAuth1Credentials(
  consumerKey,
  consumerSecret,
  accessToken,
  tokenSecret)
val request = FilterEndpoint(credentials, Track(List("#np")))
InsatiableBirdy.twitterEndpointFlow(request).runForeach(println)
```
