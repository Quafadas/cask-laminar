package example.backend

import org.ekrich.config.ConfigFactory
import org.ekrich.config.Config
import java.io.File
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import io.undertow.Undertow
import org.postgresql.ds.PGSimpleDataSource

class WithDbFramework extends utest.runner.Framework {

  var server: io.undertow.Undertow = null

  val confFile = new File("./modules/backend/src/test/scala/dev.conf")

  object IntegratedDBServer extends cask.MainRoutes with ServerT {
    override lazy val conf: Config = {
      ConfigFactory.parseFile(confFile)
    }
  }

  override def setup() = {
    println("Starting test framework")

    // only works in SBT terminal, rather than metals directly, sadly.
    println(confFile.getAbsolutePath())
    val conf: Config = ConfigFactory.parseFile(confFile)

    val pgDataSource: PGSimpleDataSource =
      new org.postgresql.ds.PGSimpleDataSource()
    pgDataSource.setUser(conf.getString("db.dbuser"))
    pgDataSource.setDatabaseName(conf.getString("db.dbname"))
    pgDataSource.setPassword(conf.getString("db.password"))
    pgDataSource.setServerName(conf.getString("db.servername"))
    pgDataSource.setPortNumber(conf.getInt("db.port"))

    server = Undertow.builder
      .addHttpListener(conf.getInt("app.port"), "localhost")
      .setHandler(IntegratedDBServer.defaultHandler)
      .build()
    server.start()

    val conn       = pgDataSource.getConnection()
    val statement  = conn.createStatement()
    val statement2 = conn.createStatement()

    val createTable = Try {
      statement2.executeUpdate(
        s"""CREATE TABLE IF NOT EXISTS todos (
            todo_id serial primary KEY,
            description text not null, 
            completed boolean not null
        );"""
      )
    }
    val truncateTable = Try {
      statement.executeUpdate(
        s"truncate table todos;"
      )
    }

    truncateTable match {
      case Failure(exception) => println(exception)
      case Success(value)     => println(s"truncated : $value")
    }
    conn.close()
  }

  override def teardown() = {
    println("-- stopping server")
    server.stop()
  }
}
