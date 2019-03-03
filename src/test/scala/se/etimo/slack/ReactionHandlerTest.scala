package se.etimo.slack
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json
import se.etimo.slack.ReactionHandler.Reaction
import se.etimo.slack.{EmojiHandler, ReactionHandler}

class ReactionHandlerTest extends FlatSpec with Matchers {

  implicit val userNameMap = Map(("u123","Erik Mmmmlm"),("u234","Jassyr Brevo"))
  val emojiHandler = new EmojiHandler
  val reactionOne =
    Reaction("+1",List("u123","u234"),2)
  val reactionTwo = Reaction("wave",List("u123","u234"),2)
  val reactions = Option(List[Reaction](reactionOne,reactionTwo))
  val reactionHandler = new ReactionHandler(emojiHandler,Option.empty)
  val resultOne = reactionHandler.buildSingleReaction(reactionOne)
  val resultTwo = reactionHandler.buildSingleReaction(reactionTwo)
  val resultFull = reactionHandler.buildReactionBlock(reactions)
  "Reaction block " should "show reactions " in {
    println(resultFull)
    assert(resultFull contains resultOne)
    assert(resultFull contains resultTwo)
    assert(!(resultFull contains  reactionOne.name ))
    assert(!(resultFull contains  reactionTwo.name ))
  }
  "Parsing Json " should " give reactions list " in {
    val messageFromSlack = """{"client_msg_id":"64c54a87-401f-4e9d-b6a1-a349ba60dd08","type":"message","text":"This is where text goes <@123> ","user":"UE5ESLLBW","ts":"1551640780.007700","reactions":[{"name":"+1","users":["U123"],"count":1}]}"""
    val reaction = reactionHandler.getReactions(Json.parse(messageFromSlack))
    assert(reaction.isDefined)
    reaction.get.length should be(1)
  }

}
