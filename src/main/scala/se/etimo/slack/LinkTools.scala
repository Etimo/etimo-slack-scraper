package se.etimo.slack

import com.sun.xml.internal.ws.api.message.AttachmentEx
import play.api.libs.json.{JsArray, JsLookupResult, JsValue, Json}
import se.etimo.slack.SlackRead.SlackSetup
import se.etimo.slack.reimplemented.FileHandler
import slack.models.Message

object LinkTools{
  case class Attachment(title:String,title_link:String,text:Option[String],
                        image_url:Option[String],thumb_url:Option[String])
  implicit val AttachemntReader = Json.reads[Attachment]
  def getAttachment(messageJson: JsValue):Option[List[Attachment]] = {
    (messageJson \ "attachments").asOpt[List[Attachment]]
  }

  def getLinkHtml(attachment: Attachment)(implicit slackSetup: SlackSetup):String = {
    if(attachment.thumb_url.isDefined){
      val url =FileHandler.checkAndDownloadUrlWithName(attachment.thumb_url.get,attachment.title)
      return s"""<div class="linkdiv"><img src="${url}" fallback="${attachment.title}"/></div>"""
    }
    if(attachment.image_url.isDefined){
      return s"""<img src="${attachment.image_url.get}" fallback="${attachment.title}"/>"""
    }
    return s"""${attachment.title}"""
  }
private def stripStringToMaxLength(text:String,maxLength:Int): String ={
  if(text.length<=maxLength){
    text
  }
  else{
    text.substring(0,maxLength-1)+"...."
  }
}
  def buildAttachmentMarkdown(attachment: Attachment)(implicit slackSetup: SlackSetup): String ={
    "\n"+s"""
         |<div class="attachment"><h4>${attachment.title}</h4><div class="text">${stripStringToMaxLength(attachment.text.getOrElse(""),400)}</div>
         |<a href="${attachment.title_link}">${getLinkHtml(attachment)}</a></div>
    """.stripMargin

  }

}
