package example.api

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Observer
import com.raquo.airstream.web.AjaxEventStream
import example.shared.Route
import org.scalajs.dom
import upickle.default.ReadWriter

/*
 * Thbis is a bit of a kooky idea, that being the API is a pure function of the a "Route" case class.
 *
 * Would have the advantage of trivially subsituting data in for test routes...
 *
 * And not writing the API layer, which is dervied entirely from our (custom :-/) route definitions.
 */
trait RouteApiT {

  val host = dom.window.location.origin
  /*
   * Should work for any route which doesn't have path segments in ... the method signature is unashamedly hauled
   * out of laminar source code...
   *
   */
  def simpleRoute[T, D](
      route: Route[T, D],
      data: D,
      timeoutMs: Int = 0,
      headers: Map[String, String] = Map.empty,
      withCredentials: Boolean = false,
      responseType: String = "",
      isStatusCodeSuccess: Int => Boolean = AjaxEventStream.defaultIsStatusCodeSuccess,
      requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
      progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
      readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  )(implicit w: ReadWriter[D], t: ReadWriter[T]): EventStream[T] = ???

  def pathSegmentedRoute[T, D](
      route: Route[T, D],
      data: D,
      timeoutMs: Int = 0,
      headers: Map[String, String] = Map.empty,
      withCredentials: Boolean = false,
      responseType: String = "",
      isStatusCodeSuccess: Int => Boolean = AjaxEventStream.defaultIsStatusCodeSuccess,
      requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
      progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
      readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  )(
      replacePathSegemnts: String => String
  )(implicit t: ReadWriter[T], d: ReadWriter[D]): EventStream[T] = {
    val newRoute = route.copy[T, D](route = replacePathSegemnts(route.route))
    simpleRoute(
      newRoute,
      data,
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      readyStateChangeObserver
    )
  }
}

object RouteApi extends RouteApiT {

  def simpleRouteNoObserverStream[T, D](route: Route[T, D]): EventStream[T] = {

    val (stream, callback) = EventStream.withCallback[T]
    val request            = AjaxEventStream.initRequest()
    request.onreadystatechange = (_: dom.Event) => {
      if (request.readyState == 4 && request.status == 200) {
        val decoded = route.decodeResponse(request.responseText)
        println(decoded)
        callback(decoded)
      } else {
        println(request.readyState)
      }
    }
    request.open(route.method.toUpperCase(), route.route)
    AjaxEventStream.sendRequest(
      request,
      route.method.toUpperCase(),
      route.route
    )
    stream
  }

  override def simpleRoute[T, D](
      route: Route[T, D],
      data: D = None,
      timeoutMs: Int = 0,
      headers: Map[String, String] = Map.empty,
      withCredentials: Boolean = false,
      responseType: String = "",
      isStatusCodeSuccess: Int => Boolean = { (_: Int) => true },
      requestObserver: Observer[dom.XMLHttpRequest] = Observer.empty,
      progressObserver: Observer[(dom.XMLHttpRequest, dom.ProgressEvent)] = Observer.empty,
      readyStateChangeObserver: Observer[dom.XMLHttpRequest] = Observer.empty
  )(implicit w: ReadWriter[D], t: ReadWriter[T]): EventStream[T] = {
    println(data)
    println(route.route)
    val result = new AjaxEventStream(
      route.method.toUpperCase(),
      s"${host}/${route.route}",
      upickle.default.write(data),
      timeoutMs,
      headers,
      withCredentials,
      responseType,
      isStatusCodeSuccess,
      requestObserver,
      progressObserver,
      readyStateChangeObserver
    ).map(req => {
      val t = req.responseText
      route.decodeResponse(t)
    })
    result
  }
}
