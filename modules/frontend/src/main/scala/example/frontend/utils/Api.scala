package example.api

import example.shared.Route
import com.raquo.airstream.core.EventStream
import upickle.default.Writer
import com.raquo.airstream.web.AjaxEventStream
import com.raquo.airstream.core.Observer
import org.scalajs.dom

/*
 * Thbis is a bit of a kooky idea, that being the API is a pure function of the a "Route" case class.
 *
 * Would have the advantage of triviall subsituting data in for test routes...
 *
 * And not writing the API layer, which is dervied entirely from our (custom :-/) route definitions.
 */
trait RouteApiT {

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
  )(implicit w: Writer[D]): EventStream[T] = ???

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
  )(implicit w: Writer[D]): EventStream[T] = {
    val newRoute = route.copy(route = replacePathSegemnts(route.route))
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
        val decoded = route.decode(request.responseText)
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
  )(implicit w: Writer[D]) = {
    println(data)
    val result = new AjaxEventStream(
      route.method.toUpperCase(),
      route.route,
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
      route.decode(t)
    })
    result
  }
}
