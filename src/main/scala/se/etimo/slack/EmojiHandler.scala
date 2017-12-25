package se.etimo.slack

import akka.actor.ActorSystem
import com.vdurmont.emoji.EmojiParser
import se.etimo.slack.SlackRead.SlackSetup;

/**
  * Lookup emojis for any :emoji: tag.
  * If an emoji with a tag is found retrieve and replace with unicode.
  */
class EmojiHandler{
  val matchEmoji = """:[a-zA-Z]+:""".r.unanchored
  def unicodeEmojis(text:String,
                    replacementUnicode:Option[Map[String,String]] = None,
                    replacementImage: Option[Map[String,String]] = None):String = {
    val build = new StringBuilder()
    build ++= text
    matchEmoji.findAllMatchIn(text).foreach(found=> {
      val emoji =found.group(0)
      replacementUnicode.getOrElse(Map()).get(emoji).foreach(replace => build.replaceAllLiterally(emoji,replace))
      replacementImage.getOrElse(Map()).get(emoji).foreach(replace => build.replaceAllLiterally(emoji,s"![$emoji]($replace)"))
      } )
    EmojiParser.parseToUnicode(build.toString())
  }
  def getSlackEmoji(slackSetup: SlackSetup)(implicit actorSystem: ActorSystem):Map[String,String] = {
    slackSetup.client.listEmojis()

  }

}
