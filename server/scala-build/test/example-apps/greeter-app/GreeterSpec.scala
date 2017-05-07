package org.greeter

import org.specs2.mutable.Spec

class Greeter {
  def greet(name: String) = s"Hello, $name"
}

class GreeterSpec extends Spec {
  "Greeter" should {
    "greet" in {
      new Greeter().greet("Mantas") must_== "Hello, Mantas"
    }
  }
}