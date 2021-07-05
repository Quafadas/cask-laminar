package example.shared

import upickle.default._



case class Route[T, +D](route:String, method:String, decode:String => T, encoder: Writer[T], 
paramReader: Option[String => D] = None)

 object TodoRoutes{   
    val listAllTodos = Route[Seq[Todos], Nothing]("api/todo", "get", read[Seq[Todos]](_:String), SeqLikeWriter(Todos.codec))
    val getATodo = Route[Option[Todos],  Nothing]("api/todo/:id", "get", read[Option[Todos]](_:String), OptionWriter(Todos.codec))
    val newTodo = Route[Int, NoIdTodo]("api/todo", "put",_.toInt, IntWriter, Some(read[NoIdTodo](_:String)))
    val updateTodo = Route[Long, Todos]("api/todo", "post",_.toLong, LongWriter, paramReader = Some(read[Todos](_:String)))
    val deleteTodo = Route[Int, Nothing]("api/todo/:id", "delete",_.toInt, IntWriter)
 }

 object SuggestionRoutes{
    val allSuggestions = Route[Seq[String], Nothing]("/get-suggestions", "get", read[Seq[String]](_:String), SeqLikeWriter( StringWriter ))
    val filterSuggestions = Route[Seq[String], GetSuggestions.MyRequest]("/get-suggestions", "post", read[Seq[String]](_:String), SeqLikeWriter( StringWriter ), Some(read[GetSuggestions.MyRequest](_:String)))
 }

case class Todos(todoId: Int, description: String, completed: Boolean)
object Todos  {  
  implicit val codec: ReadWriter[Todos] = macroRW[Todos]
}

case class NoIdTodo(description: String, completed: Boolean)
object NoIdTodo  {  
  implicit val codec: ReadWriter[NoIdTodo] = macroRW[NoIdTodo]
}