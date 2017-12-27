package se.etimo.slack

import org.scalatest.{FlatSpec, FunSuite, Matchers}
import play.api.libs.json.Json

class LinkToolsTest extends FlatSpec with Matchers {
  val jsMessageToo=Json.parse("{\"type\":\"message\",\"user\":\"U06DKBC0G\"," +
    "\"text\":\"Lean är som bekant ett buzzword i vår värld. I denna podcast belyses konceptet på ett bra sätt tycker jag:" +
    " <http://kapitalet.se/2017/12/18/lean-fran-toyota-till-sjostadsskolan/>\\nHandlar inte om utveckling specifikt men väl applicerbart även på" +
    " utvecklingsprocesser. Ex fokus på att leverera produkt så snabbt som möjligt snarare än att optimera effektivitet och produktivitet lokalt " +
    "(i.e per utvecklare) tycker jag utgör grund för eftertanke. Lyssna och fundera är mitt råd.\"," +
    "\"attachments\":[{\"title\":\"Lean – från Toyota till Sjöstadsskolan\"," +
    "\"title_link\":\"http://kapitalet.se/2017/12/18/lean-fran-toyota-till-sjostadsskolan/\"," +
    "\"fallback\":\"Lean – från Toyota till Sjöstadsskolan\"," +
    "\"image_url\":\"http://kapitalet.se/wp-content/uploads/2017/12/work-1713103_1920-1024x576.jpg\"," +
    "\"from_url\":\"http://kapitalet.se/2017/12/18/lean-fran-toyota-till-sjostadsskolan/\"," +
    "\"image_width\":444,\"image_height\":250,\"image_bytes\":125006,\"service_name\":\"kapitalet.se\",\"id\":1}]," +
    "\"ts\":\"1513841540.000175\",\"reactions\":[{\"name\":\"+1\",\"users\":[\"U29DWKE4C\"],\"count\":1}]}")
  val jsMessage = Json.parse("{\"type\":\"message\",\"user\":\"U29DWKE4C\",\"text\":\"https://github.com/graphql/graphiql\"," +
    "\"attachments\":[{\"service_name\":\"GitHub\",\"title\":\"graphql/graphiql\"," +
    "\"title_link\":\"https://github.com/graphql/graphiql\",\"text\":\"graphiql - An in-browser IDE for exploring GraphQL.\"," +
    "\"fallback\":\"GitHub: graphql/graphiql\",\"from_url\":\"https://github.com/graphql/graphiql\"," +
    "\"thumb_url\":\"https://avatars2.githubusercontent.com/u/12972006?s=400&v=4\"," +
    "\"thumb_width\":250,\"thumb_height\":250,\"service_icon\":\"https://a.slack-edge.com/bfaba/img/unfurl_icons/github.png\",\"id\":1}],\"ts\":\"1511941391.000233\"} ")
  "These things" should "extract attachment info" in {
    val opts = LinkTools.getAttachment(jsMessage)
    assert(opts.isDefined)
    assert(opts.get.size == 1)
    val opts2 = LinkTools.getAttachment(jsMessageToo)
    assert(opts2.isDefined)
    assert(opts2.get.size == 1)

  }

}
