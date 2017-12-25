package se.etimo.slack

import com.sun.xml.internal.ws.api.message.AttachmentEx
import play.api.libs.json.{JsArray, JsLookupResult, JsValue, Json}

object LinkTools{
  case class Attachment(title:String,title_link:String,text:String,thumb_url:String)
  implicit val AttachemntReader = Json.reads[Attachment]
def getAttachment(messageJson: JsValue):Option[List[Attachment]] = {
  (messageJson \ "attachments").asOpt[List[Attachment]]
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
