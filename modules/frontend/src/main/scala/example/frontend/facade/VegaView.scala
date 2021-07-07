package webcomponents.vega

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import annotation.unused

@js.native
@JSImport("vega-view", JSImport.Namespace)
class VegaView(@unused parsedSpec: js.Dynamic, @unused config: js.Dynamic) extends js.Object {

  def runAsync(): Unit = js.native

  def data(@unused s: String, @unused j: js.Dynamic): Unit = js.native

  def data(@unused s: String, @unused j: js.Array[js.Object]): Unit = js.native

  def signal(@unused s: String): js.Dynamic = js.native

  def addSignalListener(
      @unused s: String,
      @unused handler: js.Function2[String, js.Dynamic, js.Dynamic]
  ): VegaView = js.native

}
