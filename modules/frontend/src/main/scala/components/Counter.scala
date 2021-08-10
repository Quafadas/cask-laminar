package example.frontend.components

import com.raquo.laminar.api.L._
import org.scalajs.dom

class Counter private (
    val countSignal: Signal[Int],
    val node: Node
)

object Counter {
  def apply(label: String, startValue: Int = 0): Counter = {

    val incClickBus = new EventBus[dom.MouseEvent]
    val decClickBus = new EventBus[dom.MouseEvent]

    val countSignal = EventStream
      .merge(incClickBus.events.mapTo(1), decClickBus.events.mapTo(-1))
      .foldLeft(startValue)(_ + _)

    val node = div(
      className := "Counter",
      button(onClick --> decClickBus, "–"),
      child <-- countSignal.map(count => span(s" :: $count ($label) :: ")),
      button(onClick --> incClickBus, "+")
    )

    new Counter(countSignal, node)
  }
}
