package nasa4s.apod

import cats.{Applicative, ApplicativeError}
import cats.effect.Sync
import fs2.Stream
import io.circe.generic.JsonCodec
import nasa4s.core.ApiKey
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Method, Request, Status, Uri}

/** Wraps the Astronomy Picture of the Day (APOD) API
 *
 * @see [[https://api.nasa.gov/]] APOD */
trait Apod[F[_]] {
  /** Fetches APOD metadata for a given date.
   *
   * @param date The date of an APOD
   * @param hd   To work with an HD or non-HD version of an APOD; defaults to true */
  def call(date: String, hd: Boolean = true): F[Apod.Response]

  /** Downloads the bytes of an APOD as a stream, removing
   * the need to request the metadata before downloading.
   *
   * @param date The date of an APOD
   * @param hd   To work with an HD or non-HD version of an APOD; defaults to true
   * */
  def download(date: String, hd: Boolean = true): Stream[F, Byte]
}

object Apod {
  val url = "https://api.nasa.gov/planetary/apod"

  /** @todo Fix snake case naming and JSON parsing. */
  @JsonCodec
  case class Response(copyright: Option[String],
                      date: String,
                      explanation: String,
                      hdurl: Option[String],
                      media_type: String,
                      service_version: String,
                      title: String,
                      url: String)


  def apply[F[_] : Sync : ApplicativeError](client: Client[F], apiKey: ApiKey): Apod[F] = new ApodImpl[F](client, apiKey)

  private class ApodImpl[F[_] : Sync](client: Client[F], apiKey: ApiKey)(implicit F: ApplicativeError[F, Throwable]) extends Apod[F] {
    implicit val responseEntityDecoder: EntityDecoder[F, Response] = jsonOf[F, Response]

    override def call(date: String, hd: Boolean): F[Response] = {
      val uri = Uri.unsafeFromString(s"${Apod.url}?date=$date&hd=$hd&api_key=${apiKey.value}")
      val request = Request[F](Method.GET, uri)

      import cats.implicits._
      client.fetch[Response](request) { response =>
        response.status match {
          case Status.ClientError(error) => F.raiseError()
          case Status.ServerError =>
        }
        ???
      }
    }

    override def download(date: String, hd: Boolean): Stream[F, Byte] = {
      Stream
        .eval(call(date, hd))
        .filter(_.media_type == "image")
        .flatMap { response: Response =>
          // Safe `get` due to the `isDefined` check.
          val uri = if (hd && response.hdurl.isDefined) response.hdurl.get else response.url
          val request = Request[F](Method.GET, Uri.unsafeFromString(uri))

          client.stream(request).flatMap(_.body)
        }
    }
  }

}