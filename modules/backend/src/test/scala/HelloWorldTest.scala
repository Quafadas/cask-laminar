package example.backend

import cask.model.Status
import io.undertow.Undertow
import utest._

object HelloWorldTest extends TestSuite {

  object NoDBSever extends cask.MainRoutes with ServerT

  val tests = Tests {

    test("Hello world!") - withServer(NoDBSever) { host =>
      val req = requests.get(s"$host")
      assert(req.statusCode == Status.OK.code)
      assert(req.text().contains("hello"))
    }
    test("Should not be found as the route doesn't exist") - withServer(
      NoDBSever
    ) { host =>
      val req = requests.get(s"$host/shouldFail", check = false)
      assert(req.statusCode == Status.NotFound.code)
    }

  }

  def withServer[T](example: cask.main.Main)(f: String => T): T = {
    val server = Undertow.builder
      .addHttpListener(8081, "localhost")
      .setHandler(example.defaultHandler)
      .build
    server.start()
    val res =
      try f("http://localhost:8081")
      finally server.stop()
    res
  }

}
