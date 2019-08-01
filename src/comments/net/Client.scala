package comments.net

import java.net.{HttpURLConnection, URL}

import scala.io.Source

class Client(key: String, verbose: Boolean) {
  private val maxRequests: Int = if(key == null) 3 else 10
  private var requests = 0
  private var reset = 0L

  private def throttle(): Unit = {
    if (requests == maxRequests)
      Thread.sleep(Client.THROTTLE_WAIT + Math.max(0, System.currentTimeMillis() - reset))
    if(reset + 1000 < System.currentTimeMillis()) {
      reset = System.currentTimeMillis()
      requests = 0
    }
    requests += 1
  }

  private def call(address: String, tries: Int): Option[String] = {
    if(tries >= Client.MAX_RETRIES)
      return None
    throttle()
    val url = new URL(if (key != null) s"$address&api_key=$key" else address)
    try {
      val connection = Source.fromURL(url)
      val xml = connection.mkString
      connection.close()
      return Some(xml)
    } catch {
      case e: Throwable =>
        Thread.sleep(Client.HTTP_ERROR_WAIT)
        if(verbose)
          e.printStackTrace()
    }
    call(address, tries + 1)
  }

  def fetch(pmid: String): Option[String] = call(s"${Client.BASE_URL}"
    + s"efetch.fcgi?db=pubmed&rettype=xml&id=$pmid", 0)

}

object Client {
  val MAX_RETRIES = 5
  val HTTP_ERROR_WAIT = 375
  val THROTTLE_WAIT = 500
  val BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
}
