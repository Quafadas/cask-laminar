package example.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import example.shared.Todos
import io.getquill._
import org.ekrich.config.Config

class DB(conf: Config) {

  val pgDataSource = new org.postgresql.ds.PGSimpleDataSource()
  pgDataSource.setUser(conf.getString("db.dbuser"))
  pgDataSource.setDatabaseName(conf.getString("db.dbname"))
  pgDataSource.setPassword(conf.getString("db.password"))
  pgDataSource.setServerName(conf.getString("db.servername"))
  pgDataSource.setPortNumber(conf.getInt("db.port"))

  val config = new HikariConfig()
  config.setDataSource(pgDataSource)

  val ctx = new PostgresJdbcContext(SnakeCase, new HikariDataSource(config))
  import ctx._

  def allTodos() = ctx.run { query[Todos] }

  def aTodo(id: Int) = ctx.run { query[Todos].filter(_.todoId == lift(id)) }

  def addTodo(todo: Todos) = ctx.run {
    query[Todos]
      .insert(lift(todo))
      .onConflictIgnore(_.todoId)
      .returningGenerated(_.todoId)
  }

  def updateTodo(todo: Todos) = ctx.run {
    query[Todos].filter(_.todoId == lift(todo.todoId)).update(lift(todo))
  }

  def deleteTodo(id: Int) = ctx.run {
    query[Todos].filter(_.todoId == lift(id)).delete
  }

}
