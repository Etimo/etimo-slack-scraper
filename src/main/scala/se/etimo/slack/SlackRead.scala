package se.etimo.slack

import java.io.File

import com.typesafe.config.ConfigFactory
import slack.api.BlockingSlackApiClient
import akka.actor.ActorSystem
import com.vdurmont.emoji.EmojiParser
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable

object SlackRead {
  case class SlackSetup(token:String, slackChannel:String,
                        directory:String, baseTitle:String, startDate:String, client:BlockingSlackApiClient)
   case class Attachment(title:String,link:String,text:String)
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
   case class slackFile(fileId:String,
                   is_public:Boolean,
                   mime_type:String,
                   thumb_800:Option[String]=None,
                   preview:Option[String]=None,
                   previewHighlight:Option[String]=None)
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
"text":"<@U29DWKE4C> uploaded a file: <https://etimo.slack.com/files/U29DWKE4C/F846R6F53/image_uploaded_from_ios.png|Avtal för vatten> and commented: Avtalet för vatten är tex kvartalsbaserat så att den säger att det kostar 5400kr/mån är rätt fel. Får se om den justerar över tid. "
,"file":{"id":"F846R6F53","created":1511327993,"timestamp":1511327993,"name":"Image uploaded from iOS.png","title":"Avtal för vatten","mimetype":"image/png",
"filetype":"png","pretty_type":"PNG","user":"U29DWKE4C","editable":false,"size":861865,"mode":"hosted","is_external":false,"external_type":"",
"is_public":true,"public_url_shared":false,"display_as_bot":false,"username":"","url_private":"https://files.slack.com/files-pri/T044B4VDU-F846R6F53/image_uploaded_from_ios.png",
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
  implicit val system:ActorSystem = ActorSystem("etimoslack")

  val keyFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
  val postFormat = DateTimeFormat.forPattern("YYYY-MM-dd hh:mm ss")
  val oldestFormat = DateTimeFormat.forPattern("YYYY-dd-MM") //America, please stop doing this...

  case class Message(date:DateTime, name:String, text:String, dayKey:String)

  def readConfig(configFile:String="application.conf"): SlackSetup ={

    val config = ConfigFactory.parseFile(new File(configFile))
    val token = config.getString("slackToken")
    val slackChannel = config.getString("slackChannel")
    val directory = config.getString("postDirectory")
    val baseTitle = config.getString("baseTitle")
    val startDate = config.getString("startDate")

    val blockingSlackClient = BlockingSlackApiClient(token)
    SlackSetup(token,slackChannel
      ,directory,baseTitle
      ,startDate,blockingSlackClient)

  }

  def writePost(dateText:String,baseDir: String, title: String, body: String): Unit = {
    val file = scala.reflect.io.File(baseDir+"/"+dateText+"-"+title.replace(" ","-")+".MARKDOWN")
    val yamlHead ="---\nlayout: post\ntitle: "+title+"\n---\n"
    val finalPost = yamlHead+
      body
    file.writeAll(finalPost)

  }

  /**
    * TODO: Add interval option.
    * @param configFile File path to read for config.
    */
  def buildBlogPages(configFile:String ="application.conf"):Unit = {
    val config = readConfig(configFile)
    val getFrom = keyFormat.parseDateTime(config.startDate)
    val channel = config.client.listChannels()
      .find(c => c.name.equals(config.slackChannel)).get
    val messages =  config.client
      .getChannelHistory(channel.id,oldest = Option((getFrom.toDate.getTime/1000).toString)).messages
    val uidNameMap  = mutable.HashMap[String,String]()
    val betterMessages = messages.map( m => {
      println(m)
      val userId = (m \ "user").as[String]
      val name = uidNameMap.getOrElse(userId,
        config.client.getUserInfo(userId).name)
      val text = (m \ "text").as[String]
      uidNameMap.put(userId,name)
      val date = getDateForTs((m \ "ts").as[String])
      Message(date,name,EmojiHandler.unicodeEmojis(text),date.toString(keyFormat))
    })
    handleMessages(betterMessages,
      getFrom,uidNameMap.toMap
      ,config)
  }
  def handleMessages(betterMessages:Seq[Message],getFrom:DateTime,
                     uidNameMap:Map[String,String],config:SlackSetup) = {

    //Filter by time
    //val lateMessages = betterMessages.filter(bm => bm.date.toDate.getTime > getFrom.toDate.getTime)

    val mergedMessages = MergeMessages.mergeMessages(1000*60*5,betterMessages)
      .map(m=>Message(m.date, m.name,m.text,m.date.toString(keyFormat)))
    mergedMessages.foreach(m =>{
      println(m.date.toString(postFormat)+" "+m.name)
      println(m.text)})
    val daySeqMap = Map(mergedMessages.sortBy(m=>m.date.toDate.getTime)
      .map(m=>m.date.toString(keyFormat))
      .distinct.map(dk=>
    {
      (dk,mutable.ListBuffer.empty[Message])
    }) : _*)
    mergedMessages.foreach(bm => daySeqMap(bm.dayKey) += bm)
    daySeqMap.foreach(e => {
      val builder = mutable.StringBuilder.newBuilder
      e._2.sortBy(m=>m.date.toDate.getTime)
        .foreach(m => builder.append(markdownMessage(m,uidNameMap)))
      writePost(e._1,config.directory
        ,s"${config.baseTitle} ${e._1}",builder.toString())
    })
  }

  def markdownMessage(message: Message,uidmap:
  Map[String,String]): String = {
    val builder = new StringBuilder()
    builder.append("### ").append(message.name).append(" - ")
      .append(message.date.toString(postFormat)).append("s")
      .append("\n")
    builder.append(
      checkMessageForAt(message.text,uidmap)
        .replace("<"," [").replace(">","]")).append("\n")
    builder.toString()
  }
  def getDateForTs(ts:String): DateTime ={
    new DateTime(ts.substring(0,ts.indexOf(".")).toLong*1000)
  }
  def checkMessageForImage(message:String,client:BlockingSlackApiClient) : String = {
    if(message.contains("uploaded")){

    }
    ""
  }
  def checkMessageForLink(message:String) :String = {
    var returnMessage = message
    message.split("<").foreach(s => {
      val url = s.split(">")(0)
      returnMessage = returnMessage.replace(s"<$url>",s"[$url]($url)")

    })
    returnMessage
  }

  /**
    * Check a message for an @ call to another person and
    * replace it with a markdown compatible.
    * @param message
    * @param uidToNameMap
    * @return
    */
  def checkMessageForAt(message:String,uidToNameMap:Map[String,String]) :String = {
    var returnMessage = message
    message.split("<@").foreach(s => {
      val key = s.split(">")(0)
      val name = uidToNameMap.get(key)
      returnMessage = returnMessage.replace(s"<@$key>","@"+name.getOrElse("Unkown"))

    })
    returnMessage = checkMessageForLink(returnMessage)
    //returnMessage = addLineBreaks(returnMessage)
    returnMessage
  }

}