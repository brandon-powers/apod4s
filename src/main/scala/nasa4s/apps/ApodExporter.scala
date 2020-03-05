package nasa4s.apps

import cats.effect.ConcurrentEffect
import nasa4s.apod.Apod

trait ApodExporter[F[_]] {
  /** Exports a batch of APODs to a target destination */
  def export(dates: List[String]): F[Unit]
}

object ApodExporter {
  def parDownloadApodsWithIndex[F[_]](
    apod: Apod[F],
    dates: List[String],
    maxConcurrentDownloads: Int
  )(implicit F: ConcurrentEffect[F]): fs2.Stream[F, (Vector[Byte], Long)] =
    fs2.Stream
      .emits(dates)
      .evalTap(date => F.delay(println(s"Downloading and exporting $date APOD...")))
      .covary[F]
      .parEvalMapUnordered(maxConcurrentDownloads) { date =>
        apod.download(date).compile.toVector
      }
      .zipWithIndex
}