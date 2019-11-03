package nasa4s.neows

/** Wraps the Near Earth Object Web Service (nasa4s.neows.NeoWs) from NASA
 *
 * @todo Model request/response domain types.
 * @see [[https://api.nasa.gov/]] Under "Asteroids - nasa4s.neows.NeoWs" */
trait NeoWs[F[_]] {
  def feed: F[Unit]

  def lookup: F[Unit]

  def browse: F[Unit]
}

object NeoWs {
  val url = "https://api.nasa.gov/neo/rest/v1/feed"
}
