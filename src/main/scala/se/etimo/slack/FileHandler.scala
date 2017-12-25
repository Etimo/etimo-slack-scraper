package se.etimo.slack

import play.api.libs.json._

import scalaj.http.{Http, HttpOptions}
import se.etimo.slack.SlackRead.SlackSetup
import slack.api.BlockingSlackApiClient

object FileHandler {

  /*
  {"type":"message","subtype":"file_share",
  "text":"<@U44MC0WLT> uploaded a file: <https://etimo.slack.com/files/U44MC0WLT/F84JJ98TX/-.cs|Untitled>",
  "file":{"id":"F84JJ98TX","created":1511382126,"timestamp":1511382126,"name":"-.cs",
  "title":"Untitled","mimetype":"text/plain","filetype":"csharp","pretty_type":"C#",
  "user":"U44MC0WLT","editable":true,"size":744,"mode":"snippet","is_external":false,
  "external_type":"","is_public":true,"public_url_shared":false,"display_as_bot":false,"username":"",
  "url_private":"https://files.slack.com/files-pri/T044B4VDU-F84JJ98TX/-.cs",
  "url_private_download":"https://files.slack.com/files-pri/T044B4VDU-F84JJ98TX/download/-.cs",
  "permalink":"https://etimo.slack.com/files/U44MC0WLT/F84JJ98TX/-.cs",
  "permalink_public":"https://slack-files.com/T044B4VDU-F84JJ98TX-2928594f46",
  "edit_link":"https://etimo.slack.com/files/U44MC0WLT/F84JJ98TX/-.cs/edit",
  "preview":"Starting test execution, please wait...\r\n[xUnit.net 00:00:00.4044360]   Discovering: test\r\n[xUnit.net 00:00:00.4875050]   Discovered:  test\r\n[xUnit.net 00:00:00.5306630]   Starting:    test\r\n[xUnit.net 00:00:00.6852600]     test.UnitTest1.DividableByFiveIsBuzz [FAIL]\r",
  "preview_highlight":"<div class=\"CodeMirror cm-s-default CodeMirrorServer\" oncopy=\"if(event.clipboardData){event.clipboardData.setData('text/plain',window.getSelection().toString().replace(/\\u200b/g,''));event.preventDefault();event.stopPropagation();}\">\n<div class=\"CodeMirror-code\">\n<div><pre><span class=\"cm-variable\">Starting</span> <span class=\"cm-variable\">test</span> <span class=\"cm-variable\">execution</span>, <span class=\"cm-variable\">please</span> <span class=\"cm-variable\">wait</span>...</pre></div>\n<div><pre>[<span class=\"cm-variable\">xUnit</span>.<span class=\"cm-variable\">net</span> <span class=\"cm-number\">00</span>:<span class=\"cm-number\">00</span>:<span class=\"cm-number\">00.4044360</span>]   <span class=\"cm-variable\">Discovering</span>: <span class=\"cm-variable\">test</span></pre></div>\n<div><pre>[<span class=\"cm-variable\">xUnit</span>.<span class=\"cm-variable\">net</span> <span class=\"cm-number\">00</span>:<span class=\"cm-number\">00</span>:<span class=\"cm-number\">00.4875050</span>]   <span class=\"cm-variable\">Discovered</span>:  <span class=\"cm-variable\">test</span></pre></div>\n<div><pre>[<span class=\"cm-variable\">xUnit</span>.<span class=\"cm-variable\">net</span> <span class=\"cm-number\">00</span>:<span class=\"cm-number\">00</span>:<span class=\"cm-number\">00.5306630</span>]   <span class=\"cm-variable\">Starting</span>:    <span class=\"cm-variable\">test</span></pre></div>\n<div><pre>[<span class=\"cm-variable\">xUnit</span>.<span class=\"cm-variable\">net</span> <span class=\"cm-number\">00</span>:<span class=\"cm-number\">00</span>:<span class=\"cm-number\">00.6852600</span>]     <span class=\"cm-variable\">test</span>.<span class=\"cm-variable\">UnitTest1</span>.<span class=\"cm-variable\">DividableByFiveIsBuzz</span> [<span class=\"cm-variable\">FAIL</span>]</pre></div>\n</div>\n</div>\n","lines":12,"lines_more":7,"preview_is_truncated":false,"channels":["C4DJX5WLE"],"groups":[],"ims":[],
  "comments_count":0},"user":"U44MC0WLT","upload":true,"display_as_bot":false,"username":"jenspeterolsson","bot_id":null,"ts":"1511382127.000095"}

{"type":"message","subtype":"file_share",
"text":"<@U29DWKE4C> uploaded a file: <https://etimo.slack.com/files/U29DWKE4C/F846R6F53/image_uploaded_from_ios.png|Avtal för vatten> and commented:
Avtalet för vatten är tex kvartalsbaserat så att den säger att det kostar 5400kr/mån är rätt fel. Får se om den justerar över tid. "
,"file":{"id":"F846R6F53","created":1511327993,"timestamp":1511327993,"name":"Image uploaded from iOS.png","title":"Avtal för vatten","mimetype":"image/png",
"filetype":"png","pretty_type":"PNG","user":"U29DWKE4C","editable":false,"size":861865,"mode":"hosted","is_external":false,"external_type":"",
"is_public":true,"public_url_shared":false,"display_as_bot":false,
"username":"",
"url_private":"https://files.slack.com/files-pri/T044B4VDU-F846R6F53/image_uploaded_from_ios.png",
"url_private_download":"https://files.slack.com/files-pri/T044B4VDU-F846R6F53/download/image_uploaded_from_ios.png",
"thumb_64":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_64.png",
"thumb_80":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_80.png",
"thumb_360":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_360.png",
"thumb_360_w":203,"thumb_360_h":360,"thumb_480":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_480.png",
"thumb_480_w":270,"thumb_480_h":480,"thumb_160":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_160.png",
"thumb_720":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_720.png","thumb_720_w":405,"thumb_720_h":720,
"thumb_800":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_800.png","thumb_800_w":800,
"thumb_800_h":1422,"thumb_960":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_960.png",
"thumb_960_w":540,"thumb_960_h":960,"thumb_1024":"https://files.slack.com/files-tmb/T044B4VDU-F846R6F53-47be801c2a/image_uploaded_from_ios_1024.png","thumb_1024_w":576,"thumb_1024_h":1024,"image_exif_rotation":1,"original_w":1242,"original_h":2208,"permalink":"https://etimo.slack.com/files/U29DWKE4C/F846R6F53/image_uploaded_from_ios.png","permalink_public":"https://slack-files.com/T044B4VDU-F846R6F53-769146a942","channels":["C4DJX5WLE"],"groups":[],"ims":[],"comments_count":1,"initial_comment":{"id":"Fc842HUS2W","created":1511327993,"timestamp":1511327993,"user":"U29DWKE4C","is_intro":true,"comment":"Avtalet för vatten är tex kvartalsbaserat så att den säger att det kostar 5400kr/mån är rätt fel. Får se om den justerar över tid. "}},"user":"U29DWKE4C","upload":true,
"display_as_bot":false,"username":"daniel.winther","bot_id":null,"upload_reply_to":"3213C247-CBD9-49EF-9D60-E2AF0A5C1447","ts":"1511327993.000108"}
   */
  val FILE_TYPE = "file_share"

  case class SlackFileInfo(fileId:String,
                           name:String,
                           urlPrivate:String,
                           isPublic:Boolean,
                           mimeType:Option[String],
                           thumb800:Option[String]=None,
                           preview:Option[String]=None,
                           previewHighlight:Option[String]=None,
                           permalink:Option[String]=None,
                           permalinkPublic:Option[String]=None,
                          )

  implicit def jsLookupToOpt(value:JsLookupResult):Option[String] = value match{
    case(value:JsDefined) => Option(value.as[String])
    case _ => None
  }
  implicit def jsLookupToString(value:JsLookupResult):String = value match{
    case(value:JsDefined) =>value.as[String]
    case _ => ""
  }
  def generatePublicUrl(file:SlackFileInfo)(implicit slackSetup: SlackSetup):Option[SlackFileInfo] = {
    val result = Http("http://slack.com/api/files.sharedPublicURL")
      .postData("{\"file\":\""+file.fileId+"}")
      .header("Content-Type","application/json; charset=utf-8")
      .header("Authorization",s"Bearer ${slackSetup.token}")
      .option(HttpOptions.readTimeout(10000)).asString
  if(result.is2xx) {
    Option(slackFileInfoGen(Json.parse(result.body)))
  }
    else{
    None
  }

  }

  def downloadFile(file:SlackFileInfo)(implicit slackSetup: SlackSetup):Option[SlackFileInfo] = {
    val result = Http(file.urlPrivate)
      .header("Authorization","Bearer "+ slackSetup.token)
      .option(HttpOptions.readTimeout(10000)).asBytes
    if(result.is2xx) {
      Option(slackFileInfoGen(Json.parse(result.body)))
    }
    else{
      None
    }

  }
  /**
    * Checks json raw message for info on uploaded files.
    * @param messageJson
    * @return
    */
  def checkFiles(messageJson:JsValue): Option[List[SlackFileInfo]] ={
    val subType = (messageJson \ "subtype")
    subType match {
      case subType:JsDefined => {
        if(subType.as[String] == FILE_TYPE){
          val file = messageJson \ "file"
          Option(List(slackFileInfoGen(file.get)))
        }
        else{
        None
        }
      }
      case _ => None
    }
  }
  def slackFileInfoGen(js:JsValue): SlackFileInfo ={
    val slackInfo = SlackFileInfo(
      (js \ "id"),
      (js \ "name"),
      (js \ "url_private"),
      (js \ "is_public").as[Boolean],
      (js \ "mimetype"),
      (js \ "preview"),
      (js \ "preview_highlight"),
      (js \ "permalink"),
      (js \ "permalink_public")
    )
    slackInfo
  }




}
