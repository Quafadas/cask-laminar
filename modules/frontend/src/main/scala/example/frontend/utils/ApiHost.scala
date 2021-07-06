package example.api

object ApiHost {
  import org.scalajs.dom

  val scheme = dom.window.location.protocol
  val host   = dom.window.location.host

  println(s"$scheme//$host")
  s"$scheme//$host"
}
