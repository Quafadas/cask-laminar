package example.shared

import upickle.default._

/** An attempt at some sort of semi-safe routing concept between cask / laminar
  *
  * @param route string that is appended to the root path to create the url of this route
  * @param method http method, get post put etc
  * @param requestBodyReader some calls will need to send data from the clientside to the server side. This tells the server how to decode the request body.
  */
case class Route[T, +D](
    route: String,
    method: String,
    requestBodyReader: Option[String => D] = None
)(implicit val t: upickle.default.ReadWriter[T]) {
  def decodeResponse(s: String): T = upickle.default.read[T](s)
  def encoder()                    = t
}
object TodoRoutes {
  val listAllTodos: Route[Seq[Todos], Nothing] = Route[Seq[Todos], Nothing](
    "api/todo",
    "get"
  )
  val getATodo: Route[Option[Todos], Nothing] = Route[Option[Todos], Nothing](
    "api/todo/:id",
    "get"
  )
  val newTodo: Route[Int, NoIdTodo] = Route[Int, NoIdTodo](
    "api/todo",
    "put",
    Some(read[NoIdTodo](_: String))
  )
  val updateTodo: Route[Long, Todos] = Route[Long, Todos](
    "api/todo",
    "post",
    Some(read[Todos](_: String))
  )
  val deleteTodo: Route[Int, Nothing] =
    Route[Int, Nothing]("api/todo/:id", "delete")
}

object SuggestionRoutes {
  val allSuggestions: Route[Seq[String], Nothing] = Route[Seq[String], Nothing](
    "/get-suggestions",
    "get"
  )
  val filterSuggestions: Route[Seq[String], GetSuggestions.MyRequest] =
    Route[Seq[String], GetSuggestions.MyRequest](
      "/get-suggestions",
      "post",
      Some(read[GetSuggestions.MyRequest](_: String))
    )
}

case class Todos(todoId: Int, description: String, completed: Boolean)
object Todos {
  implicit val codec: ReadWriter[Todos] = macroRW[Todos]
}

case class NoIdTodo(description: String, completed: Boolean)
object NoIdTodo {
  implicit val codec: ReadWriter[NoIdTodo] = macroRW[NoIdTodo]
}
