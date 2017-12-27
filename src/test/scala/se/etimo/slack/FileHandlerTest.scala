package se.etimo.slack

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, FunSuite, Matchers}
import play.api.libs.json.Json
import se.etimo.slack.SlackRead.SlackSetup

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
     "\"thumb_600\":\"a_thumb_url_600\",\"thumb_600_w\":600,\"thumb_600_h\":1422,"+
     "\"thumb_400\":\"a_thumb_url_400\",\"thumb_400_w\":400,\"thumb_400_h\":1422,"+
     "\"thumb_800\":\"a_thumb_url_800\",\"thumb_800_w\":800,\"thumb_800_h\":1422,"+
     "\"thumb_1600\":\"a_thumb_url_1600\",\"thumb_1600_w\":1600,\"thumb_1600_h\":1422,"+
     "\"display_as_bot\":false," +
     "\"username\":\"daniel.winther\"}," +
     "\"ts\":\"1511244977.000193\"}")
val slackFake =
  SlackSetup("No"
    ,"xoxp-4147165470-263761019252-270089461457-245707ba23e9e1590210e7d51aa4442d"
    ,"directory either"
    ,"no asset directory either"
    ,"channel"
    ,DateTime.now()
    ,"Monday"
    ,null
    ,null,null,null)
  val handled = FileHandler.checkFiles(message)
  "Files should" should "Parsed into case class" in {
    assert(handled.isDefined)
    assert(handled.get.size == 1)
  }
  val file = handled.get.head
  "Thumbnails" should "be retrieved correctly" in {
    assert(file.thumb_url.isDefined)
    assert(file.thumb_url.get.contains("800"))
  }
  "Non-downloaded files should" should "contain urls but not show as downloaded" in {
      val path = FileHandler.getDownloadedFileUrl(handled.get.head)
     println(path)
      FileHandler.checkThumbDownload(handled.get.head)(slackFake) should be (false)
      FileHandler.checkDownloaded(handled.get.head)(slackFake) should be (false)
    }


}
