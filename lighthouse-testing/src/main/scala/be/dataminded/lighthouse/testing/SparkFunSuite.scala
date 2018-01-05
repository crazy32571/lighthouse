package be.dataminded.lighthouse.testing

import com.holdenkarau.spark.testing.DatasetSuiteBase
import org.scalatest.{FunSuite, Tag}

case object SparkTest            extends Tag("be.dataminded.lighthouse.testing.SparkTest")
case object SparkIntegrationTest extends Tag("be.dataminded.lighthouse.testing.SparkIntegrationTest")

/**
  * Base class for testing Spark-based applications.
  */
class SparkFunSuite extends FunSuite with DatasetSuiteBase {

  override protected implicit def reuseContextIfPossible: Boolean = true

  def sparkTest(name: String)(body: => Unit): Unit = {
    test(name, SparkTest) {
      body
    }
  }

  def sparkIntegrationTest(name: String)(body: => Unit): Unit = {
    test(name, SparkIntegrationTest) {
      body
    }
  }
}