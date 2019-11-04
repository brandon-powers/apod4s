package nasa4s.apps

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import cats.implicits._
import nasa4s.apod.Apod
import nasa4s.core.ApiKey
import org.http4s.client.blaze.BlazeClientBuilder

/** Exports APODs to AWS S3
 *
 * @param apod downloads APODs */
class ApodS3Exporter[F[_] : ConcurrentEffect](apod: Apod[F], maxConcurrentDownloads: Int) extends ApodExporter[F] {
  override def export(dates: List[String]): F[Unit] = {
    // TODO: Use fs2-blobstore to do S3 uploading here.
    // val store: Store[IO] = ???

    ApodExporter
      .parDownloadApodsWithIndex[F](apod, dates, maxConcurrentDownloads)
      // TODO: Upload to S3 concurrently.
      .compile
      .drain
  }

  object ApodS3ExporterApp extends IOApp {

    override def run(args: List[String]): IO[ExitCode] = {
      val builder = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)
      val apiKey = ApiKey("")

      builder
        .resource
        .use { client =>
          val apod = Apod[IO](client, apiKey)
          val exporter = new ApodS3Exporter[IO](apod, maxConcurrentDownloads = 3)

          exporter.export(List(
            "2019-10-20",
            "2019-10-21",
            "2019-10-22",
            "2019-10-23",
            "2019-10-24"
            ))
        }
        .flatTap(_ => IO(println("Exiting...")))
        .as(ExitCode.Success)
    }
  }

}
