package nasa4s.apod

import java.io.{BufferedOutputStream, FileOutputStream, OutputStream}

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp}
import cats.implicits._
import nasa4s.core.ApiKey
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

object ApodLocalDownloader extends IOApp {
  def localDownload[F[_]](client: Client[F], apiKey: ApiKey, fileName: String, blocker: Blocker)(
    implicit cs: ContextShift[F],
    F: ConcurrentEffect[F]
  ): F[Unit] = {
    val createOutputStream: F[OutputStream] = F.delay {
      new BufferedOutputStream(
        new FileOutputStream(fileName))
    }

    Apod[F](client, apiKey)
      .download("2019-11-02")
      .observe(
        fs2.io.writeOutputStream[F](createOutputStream, blocker)
      )
      .compile
      .drain
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val builder = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)
    val blocker = Blocker.liftExecutionContext(scala.concurrent.ExecutionContext.global)
    val apiKey = ApiKey("")

    builder
      .resource
      .use { client =>
        localDownload[IO](client, apiKey, "foo.jpg", blocker)
      }
      .as(ExitCode.Success)
  }
}
