package be.dataminded.lighthouse.datalake

import be.dataminded.lighthouse.common.Database
import be.dataminded.lighthouse.testing.SparkFunSuite
import org.apache.spark.sql.SaveMode
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

case class test_table(ID: java.lang.Integer, STR: String)

class JdbcDataLinkTest extends SparkFunSuite with Matchers with BeforeAndAfterAll {
  import spark.implicits._

  private val extraOptions = Map("MODE" -> "MYSQL")

  override protected def beforeAll(): Unit = {
    Database.inMemory("test", Map("MODE" -> "MYSQL", "user" -> "sa", "DB_CLOSE_DELAY" -> "-1")).withConnection { con =>
      // Create user and password
      con.createStatement().execute("CREATE USER IF NOT EXISTS TEST PASSWORD 'testpw'")
      con.createStatement().execute("ALTER USER TEST ADMIN TRUE")
      // Create table
      con.createStatement().execute("CREATE TABLE IF NOT EXISTS TEST_TABLE(ID INTEGER PRIMARY KEY, STR VARCHAR(50))")
      // Add initial data to table

      val statement = con.createStatement()
      (0 until 100).foreach(x => statement.addBatch(s"INSERT INTO TEST_TABLE(ID, STR) VALUES($x, '$x')"))
      statement.executeBatch()
    }
  }

  test("Reading JDBC datalink") {
    val jdbcDataLink = new JdbcDataLink(
      url = "jdbc:h2:mem:test",
      username = "TEST",
      password = "testpw",
      driver = "org.h2.Driver",
      extraProperties = extraOptions,
      table = "TEST_TABLE",
      partitionColumn = "ID"
    )
    val data = jdbcDataLink.readAs[test_table]()
    data.collect() should contain theSameElementsAs (0 until 100).map(x => test_table(x, s"$x"))
  }

  test("Append JDBC datalink") {
    val jdbcDataLink = new JdbcDataLink(
      url = "jdbc:h2:mem:test",
      username = "TEST",
      password = "testpw",
      driver = "org.h2.Driver",
      extraProperties = extraOptions,
      table = "TEST_TABLE",
      partitionColumn = "ID",
      saveMode = SaveMode.Append
    )
    val saveData = (100 until 200).map(x => test_table(x, s"$x")).toDS()
    jdbcDataLink.write(saveData)

    val data = jdbcDataLink.readAs[test_table]()
    data.collect() should contain theSameElementsAs (0 until 100).map(x => test_table(x, s"$x")) ++
      (100 until 200).map(x => test_table(x, s"$x"))
  }

  test("Overwrite JDBC datalink") {
    import spark.implicits._

    val jdbcDataLink = new JdbcDataLink(
      url = "jdbc:h2:mem:test",
      username = "TEST",
      password = "testpw",
      driver = "org.h2.Driver",
      extraProperties = extraOptions,
      table = "TEST_TABLE",
      partitionColumn = "ID",
      saveMode = SaveMode.Overwrite
    )
    val saveData = (100 until 200).map(x => test_table(x, s"$x")).toDS()
    jdbcDataLink.write(saveData)

    val data = jdbcDataLink.readAs[test_table]()
    data.collect() should contain theSameElementsAs (100 until 200).map(x => test_table(x, s"$x"))
  }
}
