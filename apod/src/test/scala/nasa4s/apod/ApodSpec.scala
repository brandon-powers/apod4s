package nasa4s.apod

import cats.effect.IO
import nasa4s.core.ApiKey
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.{AsyncFunSpec, Matchers}

class ApodSpec extends AsyncFunSpec with Matchers {
  describe("call") {
    it("calls the API and inspects the response") {
      val apiKey = ApiKey("")

      BlazeClientBuilder[IO](scala.concurrent.ExecutionContext.global)
        .resource
        .use { client: Client[IO] =>
          val callApi: IO[Apod.Response] = Apod[IO](client, apiKey).call(date = "")

          callApi.flatMap { response: Apod.Response =>
            IO(response.url shouldEqual ???)
          }
        }
    }
  }

  describe("download") {
    ???
  }
}
