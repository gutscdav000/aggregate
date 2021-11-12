package lambdablocks.aggregate.utils

import org.http4s.{Header, Headers, Uri}
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.typesafe.config.ConfigFactory
import org.typelevel.ci._

trait AuthenticationHeaders {
  private val conf = ConfigFactory.parseResources("application.conf").resolve()

  def getUnixTimestamp(): String = System.currentTimeMillis().toString

  def getUri(uri: String): Uri = Uri.fromString(uri).fold(_ => sys.error(s"Failure on uri: $uri"), identity)

  def signHash(secret: String, message: String): String = {
    val sha256_HMAC: Mac = Mac.getInstance("HmacSHA256")
    val secret_key: SecretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256")
    sha256_HMAC.init(secret_key)
    val hash = sha256_HMAC.doFinal(message.getBytes())
    Base64.getEncoder.encodeToString(hash)
  }

  def getKucoinHeaders(requestType: String, endpoint: String, body: String): Headers = {

    val apiKey = conf.getString("prod.kucoinKey")
    val secret = conf.getString("prod.kucoinSecret")
    val passphrase = conf.getString("prod.kucoinPassphrase")
    val timestamp = getUnixTimestamp()
    val message = timestamp + requestType + endpoint + body
    val base64 = signHash(secret, message)

    Headers(
      Header.Raw(ci"Content-Type", "application/json"),
      Header.Raw(ci"KC-API-KEY", apiKey),
      //        Header.Raw(ci"KC-API-KEY-VERSION", "1"),
      Header.Raw(ci"KC-API-PASSPHRASE", passphrase),
      Header.Raw(ci"KC-API-SIGN", base64),
      Header.Raw(ci"KC-API-TIMESTAMP", timestamp),
    )
  }
}
