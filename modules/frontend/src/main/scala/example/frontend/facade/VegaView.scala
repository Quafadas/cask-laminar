package webcomponents.vega

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("vega-view", JSImport.Namespace)
class VegaView(parsedSpec : js.Dynamic, config : js.Dynamic) extends js.Object {  

    def runAsync(): Unit = js.native
        
    def data(s: String, j: js.Dynamic) : Unit = js.native

    def data(s: String, j: js.Array[js.Object]) : Unit = js.native

    def signal(s: String) : js.Dynamic = js.native

    def addSignalListener(s: String, handler: js.Function2[String, js.Dynamic, js.Dynamic]) : VegaView = js.native

}

