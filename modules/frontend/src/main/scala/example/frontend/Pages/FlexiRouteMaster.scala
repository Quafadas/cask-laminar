package example.frontend

import com.raquo.laminar.api.L._

object FlexiRouteMaster {

  def apply(in: Signal[ExampleRouter.FlexiCounterPage]): Div = {

    val summarySignal = in.map { page =>
      if (page.amount <= 0) { s"Out of ${page.amount} today" }
      else if (page.amount < 5) { s"${page.countMe} colony stable" }
      else { s"${page.countMe.toUpperCase()}MAGEDDON IMMINENT!!!" }
    }

    def increment(symbol: String, increment: Int, inPage: ExampleRouter.FlexiCounterPage) = a(
      button(symbol, ExampleRouter.navigateTo(ExampleRouter.FlexiCounterPage(inPage.countMe, inPage.amount + increment)))
    )

    div(
      h1(
        child.text <-- in.map { page => s"${page.countMe}Master 9000 dashboard" }
      ),
      child <-- in.map(page => increment("-", -1, page)),
      child <-- in.map(page => span(s" :: ${page.amount} (${page.countMe}) :: ")),
      child <-- in.map(page => increment("+", 1, page)),
      br(),
      br(),
      div(child.text <-- summarySignal)
    )
  }
}
