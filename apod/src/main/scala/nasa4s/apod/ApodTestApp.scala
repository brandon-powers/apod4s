package nasa4s.apod

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.syntax._
import org.http4s.client.blaze.BlazeClientBuilder

object ApodTestApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val builder = BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)
    val blocker = Blocker.liftExecutionContext(scala.concurrent.ExecutionContext.global)

    builder
      .resource
      .use { client =>
        Apod[IO](client).call.flatMap { response => IO(println(response.asJson.spaces2)) }
        // Apod.downloadToLocalFile[IO](client, "bran-test2.jpg", blocker)
      }
      .as(ExitCode.Success)
  }
}
