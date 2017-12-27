package se.etimo.slack

import org.scalatest._

class EmojiHandlerTest extends FlatSpec with Matchers  {
  val handler:EmojiHandler = new EmojiHandler
  "Emojis in tags should be" should "be replaced with unicode" in {
    val text = handler.unicodeEmojis("I am text with wink emoji! :wink: and weary emoji :weary: :parrot: :slightly_smiling_face:")
    println(text)
    text should not contain ":wink:"
    text should not contain  ":weary:"
  }
}
