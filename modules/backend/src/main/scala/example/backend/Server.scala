package example.backend

import cask.model.Response
import cask.router.Result
import example.shared._
import org.ekrich.config.Config
import org.ekrich.config.ConfigFactory
import upickle.default._


import annotation.unused
// Split the object and trait so that the tests can have independant database implementations...
object Server extends cask.MainRoutes with ServerT {
  override lazy val conf: Config = ConfigFactory.load("application.conf")
}

trait ServerT extends cask.Routes {
  // Compile time check that the route type matches the decoder in it's route spec
  def routeTypeCheck[T, U](@unused a: T, @unused b: U)(implicit evidence: T =:= U) = true
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  lazy val conf: Config = ???
  lazy val myDB: DB     = new DB(conf)

  class jsonApi[T, D](val routeDef: Route[T, D]) extends cask.HttpEndpoint[T, Seq[String]] {

    override val path: String         = routeDef.route
    override val methods: Seq[String] = Seq(routeDef.method)

    lazy implicit val s: Writer[T] = routeDef.encoder
    def wrapFunction(ctx: cask.Request, delegate: Delegate): Result[Response[Response.Data.WritableData[String]]] = {
      delegate(Map()).map { (num: T) =>
        cask.Response(upickle.default.write(num))
      }
    }
    def wrapPathSegment(s: String) = Seq(s)
    type InputParser[T] = cask.endpoints.QueryParamReader[T]
  }

  // This is our simplest hello route, we test it to make sure :-).
  @cask.get("/")
  def hi(): String = "hello"

  @cask.get("/api/pieSpec")
  def pieSpec(): String = example.viz.Pie.pieSpec

  @jsonApi(TodoRoutes.listAllTodos)
  def allToDos(): Seq[Todos] = {
    myDB.allTodos()
  }
  def typeCheck1: Boolean =
    routeTypeCheck(allToDos(), TodoRoutes.listAllTodos.decodeResponse(""))

  @jsonApi(TodoRoutes.getATodo)
  def aTodo(id: Int): Option[Todos] = {
    myDB.aTodo(id).headOption
  }
  def t2: Boolean = routeTypeCheck(aTodo(0), TodoRoutes.getATodo.decodeResponse(""))

  @jsonApi(TodoRoutes.newTodo)
  def createToDos(request: cask.Request): Int = {
    val td: NoIdTodo = TodoRoutes.newTodo.requestBodyReader.get(request.text())
    myDB.addTodo(Todos(0, td.description, td.completed))
  }
  def t3: Boolean = routeTypeCheck(
    createToDos(cask.Request(null, Seq(""))),
    TodoRoutes.newTodo.decodeResponse("")
  )

  @jsonApi(TodoRoutes.updateTodo)
  def updateTodo(request: cask.Request): Long = {
    //println(request)
    val td = read[Todos](request.text())
    myDB.updateTodo(td)
  }
  def t4: Boolean = routeTypeCheck(
    updateTodo(cask.Request(null, Seq(""))),
    TodoRoutes.updateTodo.decodeResponse("")
  )

  @jsonApi(TodoRoutes.deleteTodo)
  def deleteToDo(id: Int): Long = {
    println(s"got delete request for $id")
    myDB.deleteTodo(id)
  }
  //----------------
  val sillySuggestions: Seq[String] =
    Seq("apple", "pear", "orange", "strawberries", "orangutan")
  @jsonApi(SuggestionRoutes.filterSuggestions)
  def suggest(request: cask.Request): Seq[String] = {
    val req = SuggestionRoutes.filterSuggestions.requestBodyReader.get(request.text())
    req.prefixOnly match {
      case Some(true) => sillySuggestions.filter(_.startsWith(req.search))
      case _          => sillySuggestions.filter(_.contains(req.search))
    }

  }
  def t6: Boolean = routeTypeCheck(
    suggest(cask.Request(null, Seq(""))),
    SuggestionRoutes.filterSuggestions.decodeResponse("")
  )

  @jsonApi(SuggestionRoutes.allSuggestions)
  def suggest(): Seq[String] = {
    sillySuggestions
  }
  def t5: Boolean = routeTypeCheck(suggest(), SuggestionRoutes.allSuggestions.decodeResponse(""))

  @cask.get("/ui", subpath = true)
  def redirectMe() = {
    val f = scala.io.Source.fromFile("src/main/resources/assets/html/Index.html")
    cask.Response(f.mkString(""), 200, Seq("Content-Type" -> "text/html"))    
  }

  @cask.staticFiles("/assets/js",  headers = Seq("Content-Type" -> "text/javascript"))
  def staticJSRoute(): String = "src/main/resources/assets/js"

  @cask.staticFiles("/assets/html",  headers = Seq("Content-Type" -> "text/html"))
  def staticHtmlRoute(): String = "src/main/resources/assets/html"

  initialize()
}
