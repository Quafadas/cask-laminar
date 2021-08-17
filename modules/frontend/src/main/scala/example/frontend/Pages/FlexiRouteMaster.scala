package example.frontend

import com.raquo.laminar.api.L._

object FlexiRouteMaster {

  def apply(in: Signal[ExampleRouter.FlexiCounterPage]): Div = {

    val summarySignal = in.map { page =>
      if (page.amount <= 0) { s"Out of ${page.amount} today" }
      else if (page.amount < 5) { s"${page.countMe} colony stable" }
      else { s"${page.countMe.toUpperCase()}MAGEDDON IMMINENT!!!" }
    }

    def pageIncrement(inPage: ExampleRouter.FlexiCounterPage, increment: Int) =
      ExampleRouter.router.absoluteUrlForPage(ExampleRouter.FlexiCounterPage(inPage.countMe, inPage.amount + increment))

    /*         def pageIncrementP(inPage: ExampleRouter.FlexiCounterPage, increment: Int) =
          ExampleRouter.FlexiCounterPage(inPage.countMe, inPage.amount + increment)
     */
    div(
      h1(
        child.text <-- in.map { page => s"${page.countMe}Master 9000 dashboard" }
      ),
      a(button("-"), href <-- in.map(inPage => pageIncrement(inPage, -1))),
      span(child.text <-- in.map(page => s" :: ${page.amount} (${page.countMe}) :: ")),
      a(
        button("+"),
        href <-- in.map(inPage => pageIncrement(inPage, 1))
      ),
      br(),
      br(),
      div(child.text <-- summarySignal)
    )
  }
}
