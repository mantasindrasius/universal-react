package lt.indrasius.builder

import org.specs2.mutable.Spec
import org.specs2.specification.Scope
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
import java.nio.file.{FileSystems, Paths, Path, FileVisitResult, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.io.{IOException}
import scala.io.Source

object ScalaBuilder {
  def main(args: Array[String]): Unit = {
    val options = ScalaBuilderOptions.parse(args)
    val builder = new ScalaBuilder(options)

    builder.build(options.sources:_*)

    if (options.commands) {
      val lines = Source.fromInputStream(System.in).getLines

      lines.foreach(line => {
        println("Re-build due to source change")
        builder.build(options.sources:_*)
      })
    }
  }
}

object ScalaBuilderOptions {
  val parser = new scopt.OptionParser[ScalaBuilderOptions]("scala-build") {
    head("scala-build", "1.0")

    arg[Seq[String]]("<sources>").unbounded.action { (sources, c) =>
      c.copy(sources = c.sources ++ sources)
    }

    opt[Seq[String]]('d', "dependency").unbounded.action { (deps, c) =>
      c.copy(deps = c.deps ++ deps)
    }

    opt[Unit]("commands").action { (deps, c) =>
      c.copy(commands = true)
    }
  }

  def parse(args: Array[String]): ScalaBuilderOptions =
    parser.parse(args, ScalaBuilderOptions()).get
}

case class ScalaBuilderOptions(sources: Seq[String] = Nil,
                               deps: Seq[String] = Nil,
                               commands: Boolean = false)

class ScalaBuilder(options: ScalaBuilderOptions) {
  private val deps = options.deps

  private lazy val classpath: String = {
    val depResolver = new DepResolver

    depResolver.resolve(deps.toSet)
  }

  def build(sources: String*): Unit = {
    val outputDirFile = new File(new File(".").getParentFile, s"target/classes")

    withExecutionTime("Compile", compile(outputDirFile, sources:_*))
    withExecutionTime("Tests", runTests(new File("."), outputDirFile))
  }

  def compile(outputDirFile: File, sources: String*): Unit =
    listOfSourceFiles(sources) match {
      case Nil =>
        println(s"Couldn't find sources for ${sources.mkString(", ")}")
      case sourceFiles =>

        outputDirFile.mkdirs()

        //compileWithInterpreter(sourceRootFile, sourceFiles, outputDirFile)
        compileWithCompiler(sourceFiles, outputDirFile)
    }

  private def compileWithInterpreter(sourceRootFile: File, sourceFiles: Seq[SourceFile], outputDirFile: File): Unit = {
    val interpreter = createInterpreter(outputDirFile)

    if (!interpreter.compileSources(sourceFiles:_*)) {
      throw new Exception("Compilation failed")
    }

    println("Summary:")

    interpreter.allDefinedNames.foreach((name) => println(name))
    interpreter.classLoader.getPackages().foreach(printPackage(_))

    val loadedClass = interpreter.classLoader.loadClass("org.greeter.Greeter")

    println(s"Loaded class: ${loadedClass.getName}")
  }

  private def compileWithCompiler(sourceFiles: Seq[File], outputDirFile: File): Unit = {
    val compiler = new MainClass()

    compiler.process(Array("-usejavacp", "-d", outputDirFile.getAbsolutePath) ++ sourceFiles.map(_.getAbsolutePath))
  }

  private def runTests(basepath: File, targetClassesFile: File): Unit = {
    val currentThread = java.lang.Thread.currentThread()
    val currentClassLoader = currentThread.getContextClassLoader

    val testsClassLoader = new URLClassLoader(Array(targetClassesFile.toURL), currentClassLoader)

    currentThread.setContextClassLoader(testsClassLoader)

    try org.specs2.runner.files.run(Array("filesrunner.basepath", basepath.getAbsolutePath, ".*Spec"), exit = false)
    finally currentThread.setContextClassLoader(currentClassLoader)
  }

  private def printPackage(pkg: Package): Unit = {
    println(pkg.getName)

    //pkg.loader.getPackages.foreach(printPackage(_))
  }

  private def createInterpreter(outputDir: File) = {
    val settings = new GenericRunnerSettings(println)

    settings.classpath.value = classpath
    settings.outdir.value = outputDir.getAbsolutePath
    settings.d.value = outputDir.getAbsolutePath
    settings.deprecation.value = true
    //settings.Ylogcp.value = true

    println(s"Output: ${settings.d.value}")

    new IMain(settings)
  }

  private def listOfSourceFiles(sources: Seq[String]): List[File] =
    expandSources(sources).toList.flatMap(source => {
      val d = new File(source)

      if (d.exists && d.isDirectory) {
          d.listFiles.filter(_.isFile).toList
      } else {
          List[File](d).filter(_.isFile)
      }
    })

  private def expandSources(sources: Seq[String]) =
    sources.flatMap {
      case source if new File(source).exists() => Seq(source)
      case source => expandSource(source)
    }

  private def expandSource(source: String): Seq[String] = {
    val matcher = FileSystems.getDefault().getPathMatcher(s"glob:$source")
    val files = Seq.newBuilder[String]

    Files.walkFileTree(Paths.get(""), new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attrs: BasicFileAttributes) = {
        if (matcher.matches(file)) {
          files += file.toString
        }

        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path, exc: IOException) = {
        FileVisitResult.CONTINUE
      }
    });

    files.result
  }

  private def toSourceFile(file: File): SourceFile = {
    val fileDir = file.getParent
    val filename = file.getName
    val content = new String(Files.readAllBytes(Paths.get(fileDir, filename)))

    new BatchSourceFile(new VirtualFile(filename, fileDir), content.toCharArray)
  }

  private def withExecutionTime(name: String, getValue: => Unit) = {
    val start = System.currentTimeMillis

    getValue

    println(s"$name took ${System.currentTimeMillis - start}ms")
  }
}

class DepResolver {
  import scalaz.{\/-, -\/}

  def resolve(deps: Set[String]): String = {
    val start = Resolution(
      deps.map(dependencyOf))

    val repositories = Seq(
      Cache.ivy2Local,
      MavenRepository("https://repo1.maven.org/maven2")
    )

    val fetch = Fetch.from(repositories, Cache.fetch())

    val resolution = start.process.run(fetch).run
    val errors: Seq[(Dependency, Seq[String])] = resolution.errors

    val localArtifacts = Task.gatherUnordered(
      resolution.artifacts.map(Cache.file(_).run)
    ).run

    val artifacts = localArtifacts.map {
      case \/-(value) => value
      case -\/(error) => throw new Exception(error.message)
    } collect {
      case file if file.getAbsolutePath.endsWith(".jar") => file
    }

    artifacts.foreach((path) => println(path))

    artifacts.mkString(":")
  }

  private def dependencyOf(dep: String): Dependency = {
    val Array(groupId, artifactId, version) = dep.split(':')

    Dependency(
      Module(groupId, artifactId), version
    )
  }
}
