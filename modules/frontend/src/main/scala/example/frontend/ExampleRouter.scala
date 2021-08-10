package example.frontend

import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import org.scalajs.dom
import upickle.default._

object ExampleRouter {

  sealed abstract class Page(val title: String)

  case object HomePage                                      extends Page("Home")
  case object DuckCounterPage                               extends Page("Duck Counter")
  case class FlexiCounterPage(countMe: String, amount: Int) extends Page("Flexi Route Counter")
  case object TodoMvcPage                                   extends Page("Todo MVC")

  implicit val HomePageRW: ReadWriter[HomePage.type]               = macroRW
  implicit val TodoMvcPageRW: ReadWriter[TodoMvcPage.type]         = macroRW
  implicit val DuckCounterPageRW: ReadWriter[DuckCounterPage.type] = macroRW
  implicit val FlexiCounterPageRW: ReadWriter[FlexiCounterPage]    = macroRW

  implicit val rw: ReadWriter[Page] = macroRW

  private val routes = List(
    Route.static(HomePage, root / endOfSegments, Router.localFragmentBasePath),
    Route.static(DuckCounterPage, root / "duck-counter" / endOfSegments, Router.localFragmentBasePath),
    Route[FlexiCounterPage, (String, Int)](
      encode = flexiPage => (flexiPage.countMe, flexiPage.amount),
      decode = arg => FlexiCounterPage(arg._1, arg._2),
      pattern = root / "flexi-counter" / segment[String] / segment[Int] / endOfSegments,
      Router.localFragmentBasePath
    ),
    Route.static(TodoMvcPage, root / "todo-mvc" / endOfSegments, Router.localFragmentBasePath)
  )

  val router = new Router[Page](
    routes = routes,
    getPageTitle = _.title,                        // displayed in the browser tab next to favicon
    serializePage = page => write(page)(rw),       // serialize page data for storage in History API log
    deserializePage = pageStr => read(pageStr)(rw) // deserialize the above
  )(
    $popStateEvent = windowEvents.onPopState, // this is how Waypoint avoids an explicit dependency on Laminar
    owner = unsafeWindowOwner                 // this router will live as long as the window
  )

  // Note: for fragment ('#') URLs this isn't actually needed.
  // See https://github.com/raquo/Waypoint docs for why this modifier is useful in general.
  def navigateTo(page: Page): Binder[HtmlElement] = Binder { el =>
    val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

    if (isLinkElement) {
      el.amend(href(router.absoluteUrlForPage(page)))
    }

    // If element is a link and user is holding a modifier while clicking:
    //  - Do nothing, browser will open the URL in new tab / window / etc. depending on the modifier key
    // Otherwise:
    //  - Perform regular pushState transition
    (onClick
      .filter(ev => !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey)))
      .preventDefault
      --> (_ => router.pushState(page))).bind(el)
  }
}
