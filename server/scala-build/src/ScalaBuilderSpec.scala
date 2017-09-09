package lt.indrasius.builder

import org.specs2.mutable.Spec
import org.specs2.specification.Scope
import org.specs2.matcher.ThrownExpectations
import scala.tools.nsc.{GenericRunnerSettings, MainClass}
import scala.tools.nsc.interpreter.IMain
import coursier._
import scalaz.concurrent.Task
import java.io.File
import reflect.internal.util.{SourceFile, BatchSourceFile}
import scala.reflect.io.VirtualFile
import java.nio.file.{Files, Paths}
import java.lang.Package
import java.net.{URLClassLoader, URL}
import scopt.OptionDef
import com.twitter.io.TempDirectory

class ScalaBuilderSpec extends Spec with ThrownExpectations {
  lazy val builder = new ScalaBuilder(ScalaBuilderOptions(deps = Seq(
    "org.scala-lang:scala-library:2.12.2",
    "org.specs2:specs2-core_2.12:3.8.9")))

  "Builder" should {
    "build and run tests" in {
      val outputDir = TempDirectory.create()

      builder.compile(outputDir, "test/example-apps/greeter-app")

      1 must_== 1
    }

    "build and run tests on a wild-card pattern" in {
      val outputDir = TempDirectory.create()

      builder.compile(outputDir, "test/example-apps/greeter-app/**.scala")

      1 must_== 1
    }
  }
}

class ScalaBuilderOptionsSpec extends Spec {
  "ScalaBuilderOptions" should {
    "parse options with single source" in {
      ScalaBuilderOptions.parse(Array("src/**")) must_==
        ScalaBuilderOptions(
          sources = Seq("src/**"),
          deps = Nil)
    }

    "parse options with multiple sources" in {
      ScalaBuilderOptions.parse(Array("src/**", "a")) must_==
        ScalaBuilderOptions(
          sources = Seq("src/**", "a"),
          deps = Nil)
    }

    "parse options with single dependency" in {
      ScalaBuilderOptions.parse(Array("-d", "dep1", "a")) must_==
        ScalaBuilderOptions(
          sources = Seq("a"),
          deps = Seq("dep1"))
    }

    "parse options with multiple dependencies" in {
      ScalaBuilderOptions.parse(Array("-d", "dep1", "-d", "dep2", "a")) must_==
        ScalaBuilderOptions(
          sources = Seq("a"),
          deps = Seq("dep1", "dep2"))
    }
  }
}
