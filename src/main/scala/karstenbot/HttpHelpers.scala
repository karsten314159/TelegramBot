package karstenbot

import scala.io.{Codec, Source}
import scala.util.{Failure, Try}

object HttpHelpers {
  /** https://alvinalexander.com/scala/how-to-write-scala-http-get-request-client-source-fromurl */
  def get(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET"): Try[String] = {
    import java.net.{HttpURLConnection, URL}
    Try {
      val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(connectTimeout)
      connection.setReadTimeout(readTimeout)
      connection.setRequestMethod(requestMethod)
      val inputStream = connection.getInputStream
      val content = Source.fromInputStream(inputStream)(Codec.UTF8).mkString
      if (inputStream != null) inputStream.close()
      content
    }
  }

  def error[T](x: Throwable): Try[T] = {
    x.printStackTrace()
    Failure(x)
  }

  def error[T](x: Any): Try[T] = {
    val msg = "Illegal: " + x
    val t = new IllegalArgumentException(msg)
    t.printStackTrace()
    error(t: Throwable)
  }
}