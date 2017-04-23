package lt.indrasius.react

import org.specs2.mutable.Specification
import javax.script.ScriptEngineManager

class HelloSpec extends Specification {
  "Hello" should {
    "should pass" in {
      val scriptEngineManager = new ScriptEngineManager()
      val nashorn = scriptEngineManager.getEngineByName("nashorn")

      1 must_== 1
    }
  }  
}