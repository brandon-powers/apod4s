package apod4s

import cats.effect.ConcurrentEffect
import com.typesafe.config.{Config, ConfigFactory}
import fs2.Stream
import io.circe.generic.JsonCodec
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Request, Uri}

/** @see [[https://api.nasa.gov/]] Under "APOD" */
trait Apod[F[_]] {
  def call: F[Apod.Response]

  def download: Stream[F, Byte]
}

object Apod {
  val url = "https://api.nasa.gov/planetary/apod"

  case class Parameters(api_key: String, date: String, hd: Boolean = true) {
    def build: String = s"date=$date&hd=$hd&api_key=$api_key"
  }

  /** @todo Fix snake case naming and JSON parsing. */
  @JsonCodec
  case class Response(copyright: String,
                      date: String,
                      explanation: String,
                      hdurl: String,
                      media_type: String,
                      service_version: String,
                      title: String,
                      url: String)


  def apply[F[_]](client: Client[F])(implicit F: ConcurrentEffect[F]): Apod[F] = new Apod[F] {
    implicit val responseEntityDecoder: EntityDecoder[F, Response] = jsonOf[F, Response]

    override def call: F[Response] = {
      val root: Config = ConfigFactory.load
      val apiKey = root.getString("apod.api-key")

      // TODO: Fix/clean-up parameter passing.
      val parameters = Parameters(apiKey, "2019-11-02")
      val uri = Uri.unsafeFromString(s"${Apod.url}?${parameters.build}")
      val request = Request[F](org.http4s.Method.GET, uri)

      client.expect[Response](request)
    }

    override def download: Stream[F, Byte] = {
      Stream
        .eval(call)
        .flatMap { response: Response =>
          val request = Request[F](org.http4s.Method.GET, Uri.unsafeFromString(response.hdurl))

          client.stream(request).flatMap(_.body)
        }
    }
  }
}