package example.frontend

import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSImport

//import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import example.api.RouteApi
import example.shared.NoIdTodo
import example.shared.TodoRoutes
import example.shared.Todos
import org.scalajs.dom
import webcomponents.vega.VegaView

import annotation.unused

@js.native
@JSImport("../../../../src/main/scala/resources/todo.css", JSImport.Namespace)
object Css extends js.Object

// Shamelessly pilfered from Laminar docs
object TodoMvcApp {
  @unused private val css = Css
  sealed abstract class Filter(val name: String, val passes: Todos => Boolean)

  object ShowAll extends Filter("All", _ => true)

  object ShowActive extends Filter("Active", !_.completed)

  object ShowCompleted extends Filter("Completed", _.completed)

  val filters: List[Filter] = ShowAll :: ShowActive :: ShowCompleted :: Nil

  sealed trait Command

  case class Create(itemText: String) extends Command

  case class UpdateText(itemId: Int, text: String) extends Command

  case class UpdateCompleted(itemId: Int, completed: Boolean) extends Command

  case class Delete(itemId: Int) extends Command

  case object DeleteCompleted extends Command

  // --- Server / State Sync helpers ---
  // This gets called a lot in order to retrieve server items and map into itemVar. Inefficient from a networking perspective!
  def updateState(): EventStream[Seq[Todos]] =
    RouteApi.simpleRoute(TodoRoutes.listAllTodos).map(_.sortBy(_.todoId))
  private val itemsVar: Var[Seq[Todos]] = Var(Seq[Todos]())

  def deleteStream(itemId: Int): EventStream[Long] =
    RouteApi.pathSegmentedRoute(TodoRoutes.deleteTodo, None) { (s: String) =>
      s.replace(":id", itemId.toString())
    }
  def updateItem(todo: Todos) =
    RouteApi.simpleRoute(TodoRoutes.updateTodo, data = todo)
  def createItemUpdateState(desc: String) = RouteApi
    .simpleRoute(TodoRoutes.newTodo, data = NoIdTodo(desc, false))
    .flatMap(_ => updateState())
  def updateStateFromStream(inStream: EventStream[_]) =
    inStream.flatMap(_ => updateState())

  // When building up streams, I unashamedly mapped them into these temporary observers...
  /*  private val testVarString : Var[String] = Var( "")
    private val testVarNoIdToDo: Var[NoIdTodo] = Var( NoIdTodo("", false) )
    private val testVarInt: Var[Int] = Var(0 )
    private val testVarSInt: Var[Seq[Int]] = Var(Seq(0) )
   */

  private val filterVar = Var[Filter](ShowAll)
  // Data Viz machinery
  val config = JSON.parse("""{"logLevel": 0}""")

  // This signal comes from a JS Promise... that's nice for third party integration.
  val vizDivPieClass = "vizPie"

  val pieStreamSt = EventStream
    .fromJsPromise(
      typings.vegaEmbed.mod.default(
        s"#$vizDivPieClass",
        s"${RouteApi.host}/api/pieSpec"
      )
    )
    .toWeakSignal

  val managePieViewObj = pieStreamSt.map {
    _.map(_.view.asInstanceOf[VegaView])
  }

  val updatePieVizStream = managePieViewObj.combineWith(itemsVar.signal)
  // --- Views ---
  def apply(): HtmlElement = {

    val $todoItems: Signal[Seq[Todos]] = itemsVar.signal
      .combineWith(filterVar.signal)
      .mapN(_ filter _.passes)
    div(
      cls("todoapp"),
      div(
        cls("header"),
        h1("Todo 14"),
        renderNewTodoInput
      ),
      div(
        hideIfNoItems,
        cls("main"),
        ul(
          cls("todo-list"),
          children <-- $todoItems.split(_.todoId)(renderTodoItem)
        )
      ),
      renderStatusBar,
      updateState() --> itemsVar.writer,
      renderViz()
    )
  }

  private def renderViz(): Div = {
    div(
      idAttr := vizDivPieClass,
      updatePieVizStream.signal --> ({
        case (view, value) => {
          val words: Map[Boolean, Seq[Todos]] = value.groupBy(_.completed)
          words.values.foreach(s => println(s.length))
          val arrayData = scala.scalajs.js.Array[scala.scalajs.js.Object]()
          println("Render this")
          arrayData.foreach(println)
          words.toVector.map {
            case (completed, items) => {
              val temp = (completed, items) match {
                case (true, items) =>
                  Dynamic.literal(field = items.length, id = "completed")
                case (false, items) =>
                  Dynamic.literal(field = items.length, id = "open")
              }
              arrayData.push(temp)
            }
          }

          dom.console.log(arrayData)
          view match {
            case Some(view) =>
              view.data("table", arrayData)
              view.runAsync()
            case _ => ()
          }
        }
      })
    )
  }

  private def renderNewTodoInput: Input =
    input(
      cls("new-todo"),
      placeholder("What needs to be done?"),
      autoFocus(true),
      inContext { thisNode =>
        composeEvents(onEnterPress)(
          _.mapTo(thisNode.ref.value)
            .filter(_.nonEmpty)
            .flatMap(s => createItemUpdateState(s))
        ) --> itemsVar.writer.contramap[Seq[Todos]] { s =>
          thisNode.ref.value = ""; s
        }
      }
    )

  // Render a single item. Note that the result is a single element: not a stream, not some virtual DOM representation.
  private def renderTodoItem(
      itemId: Int,
      @unused initialTodo: Todos,
      $item: Signal[Todos]
  ): HtmlElement = {
    val isEditingVar = Var(false) // Example of local state
    val editVar      = Var("")

    li(
      cls <-- $item.map(item => Map("completed" -> item.completed)),
      onDblClick
        .filter(_ => !isEditingVar.now())
        .mapTo(true) --> isEditingVar.writer,
      $item.map(_.description) --> editVar.writer,
      children <-- isEditingVar.signal.map[List[HtmlElement]] {
        case true => {
          renderTextUpdateInput(
            itemId,
            $item,
            isEditingVar.writer,
            editVar
          ) :: Nil
        }
        case false =>
          List(
            renderCheckboxInput(itemId, $item),
            label(child.text <-- editVar),
            // This
            button(
              cls("destroy"),
              composeEvents(onClick)(
                _.flatMap(_ => deleteStream(itemId)).flatMap(_ => updateState())
              ) --> itemsVar.writer
            )
          )
      }
    )
  }

  // Note that we pass reactive variables: `$item` for reading, `updateTextObserver` for writing
  private def renderTextUpdateInput(
      itemId: Int,
      @unused $item: Signal[Todos],
      isEditingObserver: Observer[Boolean],
      editVar: Var[String]
  ): Input = {
    input(
      cls("edit"),
      onMountFocus,
      controlled(
        value <-- editVar,
        onInput.mapToValue.filter(!_.isEmpty()) --> editVar
      ),
      composeEvents(onEnterPress)(_.mapTo {
        println("enter pressed")
        val temp: Todos = itemsVar.now().filter(_.todoId == itemId).head
        temp.copy(description = editVar.now())
      }.filter(_.description.nonEmpty).flatMap { s =>
        updateStateFromStream(updateItem(s))
      }) --> itemsVar.writer,
      List(
        onEnterPress.mapTo(false) --> isEditingObserver,
        onBlur.mapTo(false) --> isEditingObserver
      )
    )
  }

  private def renderCheckboxInput(
      itemId: Int,
      $item: Signal[Todos]
  ): Input =
    input(
      cls("toggle"),
      typ("checkbox"),
      checked <-- $item.map(_.completed),
      inContext { thisNode =>
        composeEvents(onInput)(
          _.mapTo {
            val temp: Todos = itemsVar.now().filter(_.todoId == itemId).head
            val new1: Todos = temp.copy(completed = thisNode.ref.checked)
            new1
          }
            .flatMap((s: Todos) => updateStateFromStream(updateItem(s)))
        ) --> itemsVar.writer
      }
    )

  private def renderStatusBar: Element =
    footerTag(
      hideIfNoItems,
      cls("footer"),
      span(
        cls("todo-count"),
        child.text <-- itemsVar.signal
          .map(_.count(!_.completed))
          .map(pluralize(_, "item left", "items left"))
      ),
      ul(
        cls("filters"),
        filters.map(filter => li(renderFilterButton(filter)))
      ),
      child.maybe <-- itemsVar.signal.map { items =>
        if (items.exists(ShowCompleted.passes))
          Some(
            button(
              cls("clear-completed"),
              "Clear completed",
              composeEvents(onClick)(
                _.map(_ =>
                  itemsVar
                    .now()
                    .filter(_.completed)
                    .map(_.todoId)
                )
                  .map { s => println(s); s.map(deleteStream(_)) }
                  .flatMap { (something) =>
                    EventStream
                      .sequence(something)
                      .flatMap(_ => updateState())
                  }
              ) --> itemsVar.writer
            )
          )
        else None
      }
    )

  private def renderFilterButton(
      filter: Filter
  ): ReactiveHtmlElement[org.scalajs.dom.html.Anchor] =
    a(
      cls.toggle("selected") <-- filterVar.signal.map(_ == filter),
      onClick.preventDefault.mapTo(filter) --> filterVar.writer,
      filter.name
    )

  // Every little thing in Laminar can be abstracted away
  private def hideIfNoItems: Mod[HtmlElement] =
    display <-- itemsVar.signal.map { items =>
      if (items.nonEmpty) "" else "none"
    }

  // --- Generic helpers ---

  private def pluralize(num: Int, singular: String, plural: String): String =
    s"$num ${if (num == 1) singular else plural}"

  private val onEnterPress =
    onKeyPress.filter(_.keyCode == dom.ext.KeyCode.Enter)
}
