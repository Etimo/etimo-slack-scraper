package se.etimo.slack

import org.scalatest.FunSuite

class MarkdownTweakerTest extends FunSuite {

  val test = "Dagens lösningsförslag```console bash>ls``` för prod-issue:\n```Possible solutions:\n\n- Solve the root cause of this issue in remote apis\n- Use a retry strategy for all applicable cases (problem goes away after retrying)\n- Give up and cry```gd```java public void main(String[] args){}```,"
  val testShouldResult = "Dagens lösningsförslag\n```console\n bash>ls\n```\n för prod-issue:\n```\nPossible solutions:\n\n- Solve the root cause of this issue in remote apis\n- Use a retry strategy for all applicable cases (problem goes away after retrying)\n- Give up and cry\n```\ngd\n```java\n public void main(String[] args){}\n```\n,"
  val result = MarkdownTweaker.checkMarkdownCodeBlock(test)
  println(result)
  assertResult(testShouldResult)(result)

}
