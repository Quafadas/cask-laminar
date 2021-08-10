package example.frontend

import com.raquo.laminar.api.L._
import example.frontend.components.Counter

object FlexiRouteMaster {

  def apply(countMe:String, amount : Int = 1): Div = {
    val counter = Counter(s"Number of $countMe", amount)


    val summarySignal = counter.countSignal.map { count =>
      if (count <= 0) { s"Out of $countMe today" }
      else if (count < 5) { s"$countMe colony stable"}
      else { s"${countMe.toUpperCase()}MAGEDDON IMMINENT!!!" }
    }

    div(
      h1(s"${countMe}Master 9000 Dashboard"),
      counter.node,
      br(),
      div(child.text <-- summarySignal)
    )
  }
}


