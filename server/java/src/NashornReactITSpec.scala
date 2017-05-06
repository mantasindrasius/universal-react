package lt.indrasius.react

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import javax.script.{ScriptEngine, ScriptEngineManager}

class HelloSpec extends Specification {
  class Context extends Scope {
  }

  "Hello" should {
    "should pass" in new Context {
      val scriptEngineManager = new ScriptEngineManager()
      val nashorn = scriptEngineManager.getEngineByName("nashorn")

      nashorn.loadScript("node_modules/nashorn-polyfill/dist/nashorn-polyfill.js")
      nashorn.loadScript("node_modules/react/dist/react.js")
      nashorn.loadScript("node_modules/react-dom/dist/react-dom-server.js")
      nashorn.loadScript("../../app/views/index.js")

      nashorn.eval("ReactDOMServer.renderToStaticMarkup(React.createElement(HelloMessage, { welcomeMessage: \"Welcome\" }))").toString() must
        contain("Welcome!");
    }
  }

  implicit class `Extended Nashorn`(engine: ScriptEngine) {
    def loadResourceScript(name: String) = {
      loadScript(getClass.getClassLoader.getResource(s"${name}.js").toURI.getPath)
    }
    
    def loadScript(fullPath: String) = {
      engine.eval(s"load('$fullPath')")
    }
  }
}