package se.etimo.slack

import com.sun.xml.internal.ws.api.message.AttachmentEx
import play.api.libs.json.{JsArray, JsLookupResult, JsValue, Json}
import se.etimo.slack.SlackRead.SlackSetup
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
  /*"attachments":[{"title":"Lisp In Less Than 200 Lines Of C",
  "title_link":"https://carld.github.io/2017/06/20/lisp-in-less-than-200-lines-of-c.html",
  "text":"Title: a brief and simple programming language implementationTags: lambda calculus, Lisp, C, programmingAuthors:",
  "fallback":"Lisp In Less Than 200 Lines Of C","from_url":"https://carld.github.io/2017/06/20/lisp-in-less-than-200-lines-of-c.html",
    "service_name":"carld.github.io","id":1}],"ts":"1511767741.000060"}
    {"type":"message","user":"U7RND0K7E","text":"<https://www.svd.se/allvarliga-brister-funna-i-riksbankens-it-sakerhet>",
    "attachments":
    [{"service_name":"SvD.se","title":"Allvarliga brister funna i Riksbankens it-säkerhet",
    "title_link":"https://www.svd.se/allvarliga-brister-funna-i-riksbankens-it-sakerhet",
    "text":"SvD AVSLÖJAR | Känslig information som riskerar att hamna i orätta händer, allvarliga incidentrapporter som slarvas bort,
     dåligt krypterad kommunikation och slarv i hantering av hemlig data. Riksbankens informationssäkerhet har ett flertal anmärkningsvärda brister."
     ,"fallback":"SvD.se: Allvarliga brister funna i Riksbankens it-säkerhet",
     "thumb_url":"https://images-5.svd.se/v2/images/a47ee874-bb3c-413b-9188-281dbf4621a8?q=70&w=1200&s=d8abed166f601342355410739cc977df0aeda062",
     "from_url":"https://www.svd.se/allvarliga-brister-funna-i-riksbankens-it-sakerhet",
     "thumb_width":1200,"thumb_height":800,
    "service_icon":"https://assets.svd.se/assets/images/favicon/apple-touch-icon-57x57.07773bd5.png","id":1}],"ts":"1512673667.000027"}

    */
}
