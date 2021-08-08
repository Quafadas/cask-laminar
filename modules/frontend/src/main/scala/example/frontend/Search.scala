package example.frontend

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import example.api.RouteApi
import example.shared.GetSuggestions
import example.shared.SuggestionRoutes
import org.scalajs.dom

//@JSExportTopLevel(name="Search") @JSExportAll
object Search {

  val ApiHost = example.api.ApiHost

  val searchBox: (StrictSignal[String], ReactiveHtmlElement[org.scalajs.dom.html.Input]) = {
    val zipVar           = Var("")
    val zipValueSignal   = zipVar.signal
    val zipValueObserver = zipVar.writer
    (
      zipValueSignal,
      input(
        placeholder := "Search something: ",
        controlled(
          value <-- zipValueSignal,
          onInput.mapToValue --> zipValueObserver
        )
      )
    )
  }

  val checkBox: (Var[Boolean], ReactiveHtmlElement[org.scalajs.dom.html.Input]) = {
    val checkVar = Var(true)
    (
      checkVar,
      input(
        typ := "checkbox",
        defaultChecked := checkVar.now(),
        onInput.mapToChecked --> checkVar
      )
    )
  }

  def app(debounce: Int = 250): ReactiveHtmlElement[org.scalajs.dom.html.Div] = {

    val debounced: Signal[(String, Boolean)] =
      if (debounce > 0)
        searchBox._1
          .combineWith(checkBox._1)
          .composeChanges(_.debounce(debounce))
      else searchBox._1.combineWith(checkBox._1)

    val allSuggestions: EventStream[Seq[String]] =
      RouteApi.simpleRoute(SuggestionRoutes.allSuggestions)

    val ajaxS = debounced.map { case (s, b) =>
      RouteApi.simpleRoute(
        SuggestionRoutes.filterSuggestions,
        data = GetSuggestions.MyRequest(s, Some(b))
      )
    }.flatten
    val asDomNode = ajaxS.map { ls =>
      div(ls.mkString(","))
      ul(
        for (l <- ls) yield {
          li(l)
        }
      )
    }

    val results: ReactiveHtmlElement[org.scalajs.dom.html.Div] =
      div(idAttr := "results", child <-- asDomNode)

    div(
      h1("Searchy Bit"),
      div("Search: ", searchBox._2),
      div("Prefix only", checkBox._2),
      results,
      h1("All suggestions"),
      div(
        idAttr := "allS",
        child <-- allSuggestions.map { list =>
          ul(
            for (item <- list) yield { li(item) }
          )
        }
      )
    )
  }

  //@JSExportTopLevel(name = "start", moduleID = "search")
  def renderApp(): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("appContainer"), app())
    }(unsafeWindowOwner)
  }
}
