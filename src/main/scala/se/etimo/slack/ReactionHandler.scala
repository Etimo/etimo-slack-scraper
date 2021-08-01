package se.etimo.slack

import play.api.libs.json.JsValue
import se.etimo.slack.ReactionHandler.Reaction
import play.api.libs.json._
import scala.collection.JavaConverters._

/**
  * Handles reactions for files, currently this shares code with the emoji-lookup, reactions will simply be appended
  * under the image element.
  */
object ReactionHandler{

  val classNameOuterDiv = "reactionsDiv"
  val classNameDiv = "reactionDiv"
  val classNameSpan = "reactionSpan"
  //  "reactions":[{"name":"+1","users":["U044J030G","U29DWKE4C"],"count":2}
  case class Reaction(name:String,users:List[String],count:Int)
}

class ReactionHandler(emojiHandler:EmojiHandler,emoticons:Option[Map[String,String]]) {

  implicit val readReaction = Json.reads[Reaction]

  def slackReactionToLocalReactionGen(r: com.slack.api.model.Reaction): Reaction ={
    Reaction(r.getName,r.getUsers.asScala.toList,r.getCount())
  }

  def getReactions(jsonMessage:JsValue) = {

    val lookup =(jsonMessage \ "reactions")
    if (lookup.isDefined) Option(lookup.get.as[List[Reaction]]) else Option.empty
  }

  def getReactions(message:com.slack.api.model.Message) = {
    Option(message.getReactions().asScala.map(r => slackReactionToLocalReactionGen(r)).toList)
  }

  def buildReactionBlock(reactions:Option[List[Reaction]])(implicit usernameMap:Map[String,String]):String= {
    if (reactions.isEmpty) {return ""}
    s"""
      |<div class="${ReactionHandler.classNameOuterDiv}">
      |${reactions.getOrElse(List()).foldLeft("") {
        (z,a)=>
          z+buildSingleReaction(a)
        }
      }
      </div>
    """.stripMargin

  }
  def buildSingleReaction(reaction:Reaction)(implicit usernameMap:Map[String,String]):String= {
    val emojiText = emojiHandler.translateEmoji(s":${reaction.name}:",replacementImage = emoticons)
    val names = reaction.users.fold("") {(z,nm)=>
      z+usernameMap.get(nm).map(nm => nm+", ")
        .getOrElse(", ")
    }
    s"""<div class="${ReactionHandler.classNameDiv}">
       |<span title="${names.substring(0,names.size-2)} reacted this way." class="${ReactionHandler.classNameSpan}">
       |${emojiText}x${reaction.count}</span>
       |</div>
     """.stripMargin
  }

}
