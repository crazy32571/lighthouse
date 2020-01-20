package be.dataminded.lighthouse.pipeline

import be.dataminded.lighthouse.testing.SharedSparkSession
import better.files._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TextSinkSpec extends AnyFunSpec with SharedSparkSession with Matchers with BeforeAndAfterEach {

  import spark.implicits._

  describe("TextSink") {
    it("should write the contents of a DataFrame as text") {
      val data = Seq("datadata", "datadatadata").toDF("single")

      SparkFunction.of(data).write(TextSink("./target/output/text")).run(spark)

      ("target" / "output" / "text").glob("*.txt").map(_.contentAsString).toSeq should contain theSameElementsAs Seq(
        "datadata\n",
        "datadatadata\n"
      )
    }
  }

  override protected def afterEach(): Unit = {
    ("target" / "output" / "text").delete(true)
  }
}
