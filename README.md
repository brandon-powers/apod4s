### nasa4s

`nasa4s` provides functional wrappers around [Nasa Open APIs](https://api.nasa.gov/), currently only supporting the Astronomy Picture of the Day (APOD) API.

```scala
import io.circe.syntax._

val client: Client[F] = ???
val apiKey: ApiKey = ApiKey("<nasa-developer-key")

// To fetch APOD metadata for November 2, 2019
Apod[IO](client, apiKey)
  .call(date = "2019-11-02")
  .flatMap { response: Apod.Response =>
    IO(println(response.asJson.spaces2))
  }.unsafeRunSync()

// {
//   "copyright" : "DSS",
//   "date" : "2019-11-02",
//   "explanation" : "The Flame Nebula stands out in this optical image of the dusty, crowded star forming regions toward Orion's belt, a mere 1,400 light-years away. X-ray data from the Chandra Observatory and infrared images from the Spitzer Space Telescope can take you inside the glowing gas and obscuring dust clouds though. Swiping your cursor (or clicking the image) will reveal many stars of the recently formed, embedded cluster NGC 2024, ranging in age from 200,000 years to 1.5 million years young. The X-ray/infrared composite image overlay spans about 15 light-years across the Flame's center. The X-ray/infrared data also indicate that the youngest stars are concentrated near the middle of the Flame Nebula cluster. That's the opposite of the simplest models of star formation for the stellar nursery that predict star formation begins in the denser center of a molecular cloud core. The result requires a more complex model; perhaps star formation continues longer in the center, or older stars are ejected from the center due to subcluster mergers.",
//   "hdurl" : "https://apod.nasa.gov/apod/image/1405/Flamessc2014-04a_Med.jpg",
//   "media_type" : "image",
//   "service_version" : "v1",
//   "title" : "Inside the Flame Nebula",
//   "url" : "https://apod.nasa.gov/apod/image/1405/flame_optical.jpg"
// }
```    
```scala
// To download the APOD for November 2, 2019
val client: Client[F] = ???
val apiKey: ApiKey = ApiKey("<nasa-developer-key")
val bytes: Stream[F, Byte] = Apod[IO](client, apiKey).download(date = "2019-11-02")
```

Check out `nasa4s.apps` for usage of this wrapper in an application.
