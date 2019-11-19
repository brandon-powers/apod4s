package nasa4s.apps

import java.io.{BufferedOutputStream, FileOutputStream, OutputStream}
import java.util.concurrent.Executors

import cats.effect.{Blocker, Clock, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp}
import cats.implicits._
import nasa4s.apod.Apod
import nasa4s.core.ApiKey
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

/** Exports APODs to the local filesystem
 *
 * @param apod                   downloads APODs
 * @param maxConcurrentDownloads number of concurrent APOD downloads
 * @param maxConcurrentExports   number of concurrent local APOD exports
 * @param blocker                blocking execution context */
class ApodLocalExporter[F[_]](apod: Apod[F], maxConcurrentDownloads: Int, maxConcurrentExports: Int, blocker: Blocker)(
  implicit F: ConcurrentEffect[F],
  cs: ContextShift[F]
) extends ApodExporter[F] {
  override def export(dates: List[String]): F[Unit] = {
    ApodExporter
      .parDownloadApodsWithIndex[F](apod, dates, maxConcurrentDownloads)
      .parEvalMapUnordered(maxConcurrentExports) { case (apodData: Vector[Byte], index) =>
        val createOutputStream: F[OutputStream] = F.delay {
          new BufferedOutputStream(
            // TODO: Make these names meaningful. Either the name from NASA metadata or
            //  the date would be an improvement, though this relaxed approach allows
            //  more finely-tuned concurrency control.
            new FileOutputStream(s"apod-export-$index.jpg"))
        }

        fs2.Stream
          .emits(apodData)
          .covary[F]
          .through {
            fs2.io.writeOutputStream[F](createOutputStream, blocker)
          }
          .compile
          .drain
      }.compile.drain
  }


}

object ApodLocalExporterApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val builder = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)
    val blocker = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))
    val apiKey = ApiKey("")

    builder
      .resource
      .use { client =>
        val apod = Apod[IO](client, apiKey)
        val exporter = new ApodLocalExporter[IO](apod, maxConcurrentDownloads = 3, maxConcurrentExports = 3, blocker)

        val dates = 1.to(2).flatMap { month =>
          1.to(30).map { day =>
            s"2018-$month-$day"
          }
        }.toList

        val clock = Clock.create[IO]

        for {
          start <- clock.monotonic(scala.concurrent.duration.MILLISECONDS)
          _ <- exporter.export(dates)
          end <- clock.monotonic(scala.concurrent.duration.MILLISECONDS)
        } yield start - end
      }
      .flatTap(length => IO(println(s"Exiting, took $length milliseconds...")))
      .as(ExitCode.Success)
  }
}
