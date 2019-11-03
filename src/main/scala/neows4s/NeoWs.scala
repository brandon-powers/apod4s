package neows4s

/** Wraps the Near Earth Object Web Service (NeoWs) from NASA
 *
 * @todo Model request/response domain types.
 * @see [[https://api.nasa.gov/]] Under "Asteroids - NeoWs" */
trait NeoWs[F[_]] {
  def feed: F[Unit]git q

  def lookup: F[Unit]

  def browse: F[Unit]
}

object NeoWs {
  val url = "https://api.nasa.gov/neo/rest/v1/feed"
}
