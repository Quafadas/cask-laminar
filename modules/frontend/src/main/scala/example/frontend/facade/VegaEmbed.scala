package webcomponents.vega

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object VegaEmbed {
  @js.native
  @JSImport("vega-embed", JSImport.Default)
  def embed(
      clz: String,
      spec: js.Dynamic,
      opts: js.Dynamic
  ): js.Promise[js.Dynamic] = js.native
}
