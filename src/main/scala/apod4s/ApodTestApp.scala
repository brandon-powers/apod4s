package apod4s

import java.io.{BufferedOutputStream, FileOutputStream}

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.syntax._
import org.http4s.client.blaze.BlazeClientBuilder

object ApodTestApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val builder = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)
    val blocker = Blocker.liftExecutionContext(scala.concurrent.ExecutionContext.global)

    builder.resource.use { client =>
      val createOutputStream: IO[BufferedOutputStream] = IO {
        new BufferedOutputStream(
          new FileOutputStream("bran-test.jpg")
        )
      }
      val apod = Apod[IO](client)

      val fetchAndPrintMetadata = apod.call.flatMap { response =>
        IO(println(response.asJson.spaces2))
      }

      val downloadAndWriteToLocalFile =
        apod
          .download
          .observe(
            fs2.io.writeOutputStream[IO](createOutputStream, blocker)
          )
          .compile
          .drain

      fetchAndPrintMetadata *> downloadAndWriteToLocalFile
    }.as(ExitCode.Success)
  }
}
