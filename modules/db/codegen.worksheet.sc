import $ivy.`io.getquill::quill-jdbc:3.7.0`
import $ivy.`org.postgresql:postgresql:42.2.23`
import $ivy.`io.getquill::quill-codegen-jdbc:3.7.1`
  
import io.getquill._
import io.getquill.codegen.util._
import io.getquill.codegen.model.SnakeCaseNames
import io.getquill.codegen.jdbc.SimpleJdbcCodegen
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

val pgDataSource = new org.postgresql.ds.PGSimpleDataSource()
pgDataSource.setUser("simon")
pgDataSource.setDatabaseName("todo")
pgDataSource.setPassword("") // needs password here
pgDataSource.setServerName("localhost")
pgDataSource.setPortNumber(5432)

val config = new HikariConfig()
config.setDataSource(pgDataSource)

val gen = new SimpleJdbcCodegen(pgDataSource, "example.db") {
    override def nameParser = SnakeCaseNames
}
gen.writeFiles("modules/db/gen")