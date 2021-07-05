package webcomponents.vega


import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.JSON
import scala.scalajs.js.Promise


object VegaEmbed {  
        @js.native
        @JSImport("vega-embed", JSImport.Default)
        def embed(clz : String, spec : js.Dynamic, opts : js.Dynamic) : js.Promise[js.Dynamic] = js.native
}


