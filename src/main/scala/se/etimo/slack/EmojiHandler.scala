package se.etimo.slack

import com.vdurmont.emoji.{Emoji, EmojiManager, EmojiParser};

/**
  * Lookup emojis for any :emoji: tag.
  * If an emoji with a tag is found retrieve and replace with unicode.
  */
object EmojiHandler{
  val matchEmoji = """:([a-zA-Z]+):""".r.unanchored
  def unicodeEmojis(text:String):String = {
        EmojiParser.parseToUnicode(text)
  }
}
class EmojiHandler {

}
