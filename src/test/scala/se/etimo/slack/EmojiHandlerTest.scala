package se.etimo.slack

import org.scalatest._

class EmojiHandlerTest extends FlatSpec with Matchers  {

  "Emojis in tags should be" should "be replaced with unicode" in {
    val text = EmojiHandler.unicodeEmojis("I am text with wink emoji! :wink: and weary emoji :weary:")
    println(text)
    text should not contain ":wink:"
    text should not contain  ":weary:"
  }
}
