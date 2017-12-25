package se.etimo.slack

import org.scalatest.{FlatSpec, FunSuite, Matchers}
import play.api.libs.json.Json

class FileHandlerTest extends FlatSpec with Matchers {
 val message =Json.parse(
   "{\"type\":\"message\",\"subtype\":\"file_share\"," +
     "\"text\":\"So... much...text...\"," +
     "\"file\":{\"id\":\"F82QPU49E\",\"created\":1511244978," +
     "\"timestamp\":1511244978,\"name\":\"Image uploaded from iOS.png\"," +
     "\"title\":\"Dreams spara pengar\",\"mimetype\":\"image/png\"," +
     "\"filetype\":\"png\",\"pretty_type\":\"PNG\",\"user\":\"U29DWKE4C\"," +
     "\"editable\":false,\"size\":382467,\"mode\":\"hosted\",\"is_external\":false," +
     "\"external_type\":\"\",\"is_public\":true,\"public_url_shared\":false," +
     "\"display_as_bot\":false,\"username\":\"user\"," +
     "\"url_private\":\"a_url\"," +
     "\"url_private_download\":\"a_url\"," +
     "\"thumb_800\":\"a_thumb_url\",\"thumb_800_w\":800,\"thumb_800_h\":1422,"+
     "\"display_as_bot\":false," +
     "\"username\":\"daniel.winther\"}," +
     "\"ts\":\"1511244977.000193\"}")

  "Files should" should "Parsed into case class" in {
    val handled = FileHandler.checkFiles(message)
    assert(handled.isDefined)
    assert(handled.get.size == 1)
  }
}
