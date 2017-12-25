package se.etimo.slack

import org.scalatest.{FlatSpec, FunSuite, Matchers}
import play.api.libs.json.Json

class LinkToolsTest extends FlatSpec with Matchers {
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

  }

}
