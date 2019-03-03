package se.etimo.slack

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.vdurmont.emoji.EmojiParser
import se.etimo.slack.SlackRead.SlackSetup

import scala.collection.mutable;

/**
  * Lookup emojis for any :emoji: tag.
  * If an emoji with a tag is found retrieve and replace with unicode.
  */
class EmojiHandler{
  private def getEmojiReplaceMap ={
    val emojiMap =  mutable.HashMap.empty[String,String]
    val entries = ConfigFactory.load("emojireplace").getObject("emoji").entrySet().forEach(e=>{
      emojiMap.put(e.getKey,e.getValue.unwrapped().toString)
    })
    emojiMap.toMap
  }
  val replaceEmojis = getEmojiReplaceMap

  private val matchEmoji = """:[^:\n/]+:""".r.unanchored
  def unicodeEmojis(text:String,
                    replacementUnicode:Option[Map[String,String]] = Option(replaceEmojis),
                    replacementImage: Option[Map[String,String]] = None):String = {
    var replaced = text;
    matchEmoji.findAllMatchIn(text).foreach(found=> {
      val emoji = found.group(0)
      replaced = replaceEmojiInText(replaced,emoji,replacementUnicode,replacementImage)
      })
    EmojiParser.parseToUnicode(replaced)
  }
  def getSlackEmoji(slackSetup: SlackSetup)(implicit actorSystem: ActorSystem):Map[String,String] = {
    slackSetup.client.listEmojis()

  }
   def translateEmoji(emoji:String,
                                 replacementUnicode:Option[Map[String,String]] = Option(replaceEmojis),
                                 replacementImage: Option[Map[String,String]] = None):String ={
    val replace = replaceEmojiInText(emoji,emoji,replacementUnicode,replacementImage)
    EmojiParser.parseToUnicode(replace)
  }
  private def replaceEmojiInText(text:String,emoji:String,
                    replacementUnicode:Option[Map[String,String]] = Option(replaceEmojis),
                    replacementImage: Option[Map[String,String]] = None):String ={
    var replaced = text;
    replacementUnicode.getOrElse(Map()).get(emoji).foreach(replace =>
      replaced = replaced.replaceAllLiterally(emoji, replace))
    replacementImage.getOrElse(Map()).get(emoji).foreach(replace =>
      replaced=replaced.replaceAllLiterally(emoji, s"![$emoji]($replace)"))
    return replaced;
  }

}
