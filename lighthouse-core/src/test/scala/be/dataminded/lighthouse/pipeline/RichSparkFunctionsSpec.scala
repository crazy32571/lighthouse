package be.dataminded.lighthouse.pipeline

import java.io.ByteArrayOutputStream

import be.dataminded.lighthouse.testing.SharedSparkSession
import better.files._
import org.apache.spark.sql.Dataset
import org.apache.spark.storage.StorageLevel
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RichSparkFunctionsSpec extends AnyFunSpec with Matchers with SharedSparkSession with BeforeAndAfter {

  import spark.implicits._

  describe("SparkFunctions with a DataSet inside should have extra functionality") {

    val function = SparkFunction.of(Seq(1, 2, 3, 4, 5).toDS())

    it("can cache") {
      function.cache().run(spark).storageLevel should equal(StorageLevel.MEMORY_ONLY)
    }

    it("can drop the cache") {
      function.cache().dropCache().run(spark).storageLevel should equal(StorageLevel.NONE)
    }

    it("can be written to a sink") {
      function.write(OrcSink("target/output/orc")).run(spark)

      file"target/output/orc".exists should be(true)
    }

    it("can be written to multiple sinks") {
      function.write(OrcSink("target/output/orc"), OrcSink("target/output/orc2")).run(spark)

      file"target/output/orc".exists should be(true)
      file"target/output/orc2".exists should be(true)
    }

    it("is being cached when writing to multiple sinks for performance") {
      val result = function.write(OrcSink("target/output/orc"), OrcSink("target/output/orc2")).run(spark)

      result.storageLevel should equal(StorageLevel.MEMORY_ONLY)
    }

    it("can easily be counted") {
      function.count().run(spark) should equal(5)
    }

    it("can print the schema") {
      val stream = new ByteArrayOutputStream()
      Console.withOut(stream) {
        function.printSchema().run(spark)
      }
      stream.toString() should include("value: integer (nullable = false)")
    }

    it("can be be used as a Dataset") {
      function.as[Int].run(spark) shouldBe a[Dataset[_]]
    }
  }

  after {
    file"target/output/orc".delete(true)
    file"target/output/orc2".delete(true)
  }
}
