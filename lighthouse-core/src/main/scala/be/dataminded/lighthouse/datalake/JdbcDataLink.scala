package be.dataminded.lighthouse.datalake

import org.apache.spark.sql.{DataFrame, Dataset, SaveMode}

/**
  * Default JDBC DataRef implementation for reading and writing to a JDBC database
  *
  * @param url Function returning the URL of the database you want to connect to. Should be in the following format
  *            jdbc:mysql://${jdbcHostname}:${jdbcPort}/${jdbcDatabase}
  * @param username Function returning the Username of the database you want to connect to
  * @param password Function returning the Password of the database you want to connect to
  * @param driver Function returning the Driver to use for the database you want to connect to, should be available in
  *               the classpath
  * @param table Function returning the Table of the database where you would like to write to.
  */
class JdbcDataLink(url: LazyConfig[String],
                   username: LazyConfig[String],
                   password: LazyConfig[String],
                   driver: LazyConfig[String],
                   table: LazyConfig[String],
                   extraProperties: Map[String, String] = Map.empty,
                   saveMode: SaveMode = SaveMode.Overwrite)
    extends DataLink {

  require(url() != null && url().length > 0)
  require(driver() != null && driver().length > 0)
  require(table() != null && table().length > 0)

  // build the connection properties
  private[this] lazy val connectionProperties = {
    Map(
      "url"      -> url(),
      "driver"   -> driver(),
      "table"    -> table(),
      "user"     -> username(),
      "password" -> password()
    ) ++ extraProperties
  }

  override def read(): DataFrame = {
    spark.read.jdbc(connectionProperties("url"), connectionProperties("table"), connectionProperties)
  }

  override def write[T](dataset: Dataset[T]): Unit = {
    dataset.write.mode(saveMode).jdbc(connectionProperties("url"), connectionProperties("table"), connectionProperties)
  }
}
