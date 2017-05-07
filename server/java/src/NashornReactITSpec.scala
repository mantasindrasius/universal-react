package lt.indrasius.react

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import javax.script.{ScriptContext, ScriptEngine, ScriptEngineManager, SimpleScriptContext}
import jdk.nashorn.api.scripting.{NashornScriptEngineFactory, ScriptObjectMirror}
import io.Source

class NashornReactITSpec extends Specification {
  sequential

  val nashornScriptEngineFactory = new NashornScriptEngineFactory()
  lazy val nashorn = {
      val engine = nashornScriptEngineFactory.getScriptEngine("--language=es6")
      engine.loadScript("classpath:jvm-npm.js")
      engine.loadScript("node_modules/nashorn-polyfill/dist/nashorn-polyfill.js")
      engine.require("babel-standalone")

      engine
  }

  class Context extends Scope {
  }

  "React on Nashorn" should {
    "should render" in new Context {
      nashorn.loadTranspiledScript("../../app/views/index.jsx")

      nashorn.eval("var ReactDOMServer = require('react-dom/server'); var React = require('react'); ReactDOMServer.renderToStaticMarkup(React.createElement(HelloMessage, { welcomeMessage: \"Welcome\" }))").toString() must
        contain("Welcome!");
    }
  }

  implicit class `Extended Nashorn`(engine: ScriptEngine) {
    private val JSON = engine.get("JSON").asInstanceOf[ScriptObjectMirror]

    def loadResourceScript(name: String) = {
      loadScript(getClass.getClassLoader.getResource(s"${name}.js").toURI.getPath)
    }
    
    def loadScript(fullPath: String) = {
      engine.eval(s"load('$fullPath')")
    }

    def loadTranspiledScript(fullPath: String) = {
      val source = Source.fromFile(fullPath).mkString

      engine.put("sourceForTranspile", source);

      val transpiledSource = engine.eval("var Babel = require('babel-standalone'); Babel.transform(sourceForTranspile, { presets: ['react', 'es2015'] }).code");
      val escapedSource = JSON.callMember("stringify", transpiledSource).toString()

      engine.eval(s"""load({script:$escapedSource,name:"$fullPath"})""")
    }
    
    def require(name: String) = {
      engine.eval(s"require('$name')")
    }
  }
}