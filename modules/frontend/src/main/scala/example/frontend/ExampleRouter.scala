package example.frontend

import com.raquo.laminar.api.L._
import com.raquo.laminar.api.L
import com.raquo.waypoint._
import org.scalajs.dom
import upickle.default._
import com.raquo.laminar.tags.HtmlTag

object ExampleRouter {

  sealed abstract class Page(val title: String)

  case object HomePage                                      extends Page("Home")
  case object DuckCounterPage                               extends Page("Duck Counter")
  case object SearchPage                                    extends Page("Search")
  case class FlexiCounterPage(countMe: String, amount: Int) extends Page("Flexi Route Counter")
  case object TodoMvcPage                                   extends Page("Todo MVC")

  implicit val HomePageRW: ReadWriter[HomePage.type]               = macroRW
  implicit val TodoMvcPageRW: ReadWriter[TodoMvcPage.type]         = macroRW
  implicit val SearchPageRW: ReadWriter[SearchPage.type]           = macroRW
  implicit val DuckCounterPageRW: ReadWriter[DuckCounterPage.type] = macroRW
  implicit val FlexiCounterPageRW: ReadWriter[FlexiCounterPage]    = macroRW

  implicit val rw: ReadWriter[Page] = macroRW

  val basePath = s"/ui/#"

  private val routes = List(
    Route.static(HomePage, root / endOfSegments, basePath),
    Route.static(DuckCounterPage, root / "duck-counter" / endOfSegments, basePath),
    Route[FlexiCounterPage, (String, Int)](
      encode = flexiPage => (flexiPage.countMe, flexiPage.amount),
      decode = arg => FlexiCounterPage(arg._1, arg._2),
      pattern = root / "flexi-counter" / segment[String] / segment[Int] / endOfSegments,
      basePath
    ),
    Route.static(TodoMvcPage, root / "todo-mvc" / endOfSegments, basePath),
    Route.static(SearchPage, root / "search" / endOfSegments, basePath)
  )

  val router = new Router[Page](
    routes = routes,
    getPageTitle = _.title,                        // displayed in the browser tab next to favicon
    serializePage = page => write(page)(rw),       // serialize page data for storage in History API log
    deserializePage = pageStr => read(pageStr)(rw) // deserialize the above
  )(
    popStateEvents = L.windowEvents(_.onPopState), // this is how Waypoint avoids an explicit dependency on Laminar
    owner = L.unsafeWindowOwner                 // this router will live as long as the window
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
