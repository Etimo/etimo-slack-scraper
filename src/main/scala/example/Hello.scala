package example

object Hello extends Greeting with App {
  println(greeting)
  val hello = "Hello";
}

trait Greeting {
  lazy val greeting: String = "hello"
}
