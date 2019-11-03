package apod

import java.io.{BufferedOutputStream, FileOutputStream}

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.generic.JsonCodec
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.blaze._
import org.http4s.{EntityDecoder, Request, Uri}

/** @see [[https://api.nasa.gov/]] Under "APOD" */
trait APOD[F[_]] {
  def call: F[APOD.Response]

  def download(fileName: String): F[Unit]
}

object APOD {
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


  def apply[F[_]](client: Client[F])(implicit F: ConcurrentEffect[F]): APOD[F] = new APOD[F] {
    implicit val responseEntityDecoder: EntityDecoder[F, Response] = jsonOf[F, Response]

    override def call: F[Response] = {
      val root: Config = ConfigFactory.load
      val apiKey = root.getString("apod.api-key")

      val parameters = Parameters(apiKey, "2019-11-02")
      val uri = Uri.unsafeFromString(s"${APOD.url}?${parameters.build}")
      val request = Request[F](org.http4s.Method.GET, uri)

      client.expect[Response](request)
    }

    override def download(fileName: String): F[Unit] = {
      call.flatMap { response =>
        val bytes: F[Array[Byte]] = client.expect[Array[Byte]](response.hdurl)

        bytes.flatMap { unwrappedBytes: Array[Byte] =>
          val bos = new BufferedOutputStream(new FileOutputStream(fileName))
          F.delay(bos.write(unwrappedBytes)) *> F.delay(bos.close())
        }
      }
    }
  }
}

object APODApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val builder = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)

    builder.resource.use { client =>
      APOD[IO](client).call.flatMap { response: APOD.Response =>
        IO(println(response.asJson.spaces2))
      }
    }.as(ExitCode.Success)
  }
}