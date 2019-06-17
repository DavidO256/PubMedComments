package comments.net

import java.io.IOException
import java.net.{HttpURLConnection, URL}

import scala.io.Source

class Client(key: String, requestLimit: Int) {
  private val maxRequests: Int = if(key == null) 3 else requestLimit
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

  @throws[IOException]
  private def call(address: String, tries: Int): Option[String] = {
    if(tries >= Client.MAX_RETRIES)
      return None
    throttle()
    val connection = new URL(if (key != null) s"$address&api_key=$key"
      else address).openConnection().asInstanceOf[HttpURLConnection]
    try {
      connection.setRequestMethod("GET")
      connection.connect()
      val xml = Source.fromInputStream(connection.getInputStream).mkString
      connection.disconnect()
      return Some(xml)
    } catch {
      case _: IOException => connection.getResponseCode match {
          case 429 => Thread.sleep(Client.HTTP_429_WAIT)
          case 404 => return None
        }
    }
    call(address, tries + 1)
  }

  def fetch(pmid: String): Option[String] = call(s"${Client.BASE_URL}"
    + s"efetch.fcgi?db=pubmed&rettype=xml&id=$pmid", 0)

}

object Client {
  val MAX_RETRIES = 5
  val HTTP_429_WAIT = 250
  val THROTTLE_WAIT = 500
  val BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
}
