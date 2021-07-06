package example.backend

import example.shared._
import org.ekrich.config.Config
import org.ekrich.config.ConfigFactory
import upickle.default._
// Split the object and trait so that the tests can have independant database implementations...
object Server extends cask.MainRoutes with ServerT {
  override lazy val conf: Config = ConfigFactory.load("application.conf")
}

trait ServerT extends cask.Routes {
  // Compile time check that the route type matches the decoder in it's route spec
  def routeTypeCheck[T, U](a: T, b: U)(implicit evidence: T =:= U) = true
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  lazy val conf: Config = ???
  lazy val myDB         = new DB(conf)

  class jsonApi[T, D](val routeDef: Route[T, D])
      extends cask.HttpEndpoint[T, Seq[String]] {

    override val path: String         = routeDef.route
    override val methods: Seq[String] = Seq(routeDef.method)

    lazy implicit val s: Writer[T] = routeDef.encoder
    def wrapFunction(ctx: cask.Request, delegate: Delegate) = {
      delegate(Map()).map { (num: T) =>
        cask.Response(upickle.default.write(num))
      }
    }
    def wrapPathSegment(s: String) = Seq(s)
    type InputParser[T] = cask.endpoints.QueryParamReader[T]
  }
  ///-------

  // This is our simplest hello route, we test it to make sure :-).
  @cask.get("/")
  def hi() = "hello"

  @jsonApi(TodoRoutes.listAllTodos)
  def allToDos(): Seq[Todos] = {
    myDB.allTodos()
  }
  def typeCheck1 =
    routeTypeCheck(allToDos(), TodoRoutes.listAllTodos.decode(""))

  @jsonApi(TodoRoutes.getATodo)
  def aTodo(id: Int): Option[Todos] = {
    myDB.aTodo(id).headOption
  }
  def t2 = routeTypeCheck(aTodo(0), TodoRoutes.getATodo.decode(""))

  @jsonApi(TodoRoutes.newTodo)
  def createToDos(request: cask.Request): Int = {
    val td: NoIdTodo = TodoRoutes.newTodo.paramReader.get(request.text())
    myDB.addTodo(Todos(0, td.description, td.completed))
  }
  def t3 = routeTypeCheck(
    createToDos(cask.Request(null, Seq(""))),
    TodoRoutes.newTodo.decode("")
  )

  @jsonApi(TodoRoutes.updateTodo)
  def updateTodo(request: cask.Request): Long = {
    //println(request)
    val td = read[Todos](request.text())
    myDB.updateTodo(td)
  }
  def t4 = routeTypeCheck(
    updateTodo(cask.Request(null, Seq(""))),
    TodoRoutes.updateTodo.decode("")
  )

  @cask.delete("/api/todo/:id")
  def deleteToDo(id: Int): Long = {
    println(s"got delete request for $id")
    myDB.deleteTodo(id)
  }
  //----------------
  val sillySuggestions: Seq[String] =
    Seq("apple", "pear", "orange", "strawberries", "orangutan")
  @jsonApi(SuggestionRoutes.filterSuggestions)
  def suggest(request: cask.Request): Seq[String] = {
    val req = SuggestionRoutes.filterSuggestions.paramReader.get(request.text())
    req.prefixOnly match {
      case Some(true) => sillySuggestions.filter(_.startsWith(req.search))
      case _          => sillySuggestions.filter(_.contains(req.search))
    }

  }
  def t6 = routeTypeCheck(
    suggest(cask.Request(null, Seq(""))),
    SuggestionRoutes.filterSuggestions.decode("")
  )

  @jsonApi(SuggestionRoutes.allSuggestions)
  def suggest(): Seq[String] = {
    sillySuggestions
  }
  def t5 = routeTypeCheck(suggest(), SuggestionRoutes.allSuggestions.decode(""))

  @cask.staticResources("/search")
  def searchUi() = "assets/Search.html"

  @cask.staticResources("/todo")
  def todoUi() = "assets/Todo.html"

  @cask.staticResources("/assets")
  def staticResourceRoute() = "assets"

  initialize()
}
