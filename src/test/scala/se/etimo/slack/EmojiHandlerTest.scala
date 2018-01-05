package se.etimo.slack

import org.scalatest._

class EmojiHandlerTest extends FlatSpec with Matchers  {
  val handler:EmojiHandler = new EmojiHandler
  val exampleEdgeCase = "<https://meltdownattack.com/>\nInnehåller även info om Spectre.\nSenaste nytt inom data om nån missat det :slightly_smiling_face:"
  val baseText = "I am text with wink emoji! :wink: and weary emoji :weary:"
  val finalText = handler.replaceEmojis.foldLeft(baseText)((accumulator,e)=>{s"$accumulator ${e._1}"})
  println(finalText)
  "Emojis in tags should be" should "be replaced with unicode" in {
    val replaceText = handler.unicodeEmojis(finalText)
    println(replaceText)
    replaceText should not contain ":wink:"
    replaceText should not contain  ":weary:"
    handler.replaceEmojis.foreach(e => replaceText should not contain e._2)
  }
  "Edge case with URL before emoji " should "be fixed" in {
    val problemEmoji = ":slightly_smiling_face:"
    val replaceText = handler.unicodeEmojis(exampleEdgeCase)
    println(replaceText)
    assert(!replaceText.contains(problemEmoji)==handler.replaceEmojis.contains(problemEmoji))
  }
}
