package example.backend

import cask.model.Status
import example.shared.TodoRoutes
import example.shared.Todos
import upickle.default.read
import upickle.default.write
import utest.TestSuite
import utest.Tests
import utest.test

object IntegrationTest extends TestSuite {

  var theId                 = 0
  var withId: Option[Todos] = None

  val tests = Tests {
    test("with database") {

      val host = "http://localhost:8082"
      test("that we start with an empty list of todos") {
        val req = requests.get(s"$host/api/todo")
        assert(req.statusCode == Status.OK.code)
        assert(req.text().contains("[]"))
      }

      test("that we can create a new todo") {
        val newTodo = Todos(0, "Run tests", false)
        val req = requests.put(
          s"$host/${TodoRoutes.newTodo.route}",
          data = write(newTodo)
        )
        theId = read[Int](req.text())
        assert(req.statusCode == Status.OK.code)
      }

      test("we can retrieve our new todo") {
        val req2 = requests.get(s"$host/api/todo/$theId")
        withId = TodoRoutes.getATodo.decodeResponse(req2.text())
        assert(withId.get.description.contains("Run tests"))
      }
      test("that we can update our todo") {
        val theCopy =
          withId.get.copy(completed = true, description = "modify a todo")
        val req3 = requests.post(
          s"$host/${TodoRoutes.updateTodo.route}",
          data = write(theCopy)
        )
        assert(req3.statusCode == Status.OK.code)
      }
      test("that our modification was successful") {
        val req4 = requests.get(s"$host/api/todo/$theId")
        assert(req4.statusCode == Status.OK.code)
        val singleToDoChanged = req4.text()
        assert(singleToDoChanged.contains("modify"))
      }
      test("that we can delete our todo") {
        val req5 = requests.delete(s"$host/api/todo/$theId")
        assert(req5.statusCode == Status.OK.code)

        val req6 = requests.get(s"$host/api/todo/$theId")
        assert(req6.statusCode == Status.OK.code)
        assert(req6.text == "[]")
      }
    }
  }
}
