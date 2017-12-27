package se.etimo.slack

import java.io.File

import com.typesafe.config.ConfigFactory
import slack.api.BlockingSlackApiClient
import akka.actor.ActorSystem
import org.joda.time.{DateTime, DateTimeZone, Duration}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.JsValue
import se.etimo.slack.FileHandler.SlackFileInfo
import se.etimo.slack.LinkTools.Attachment
import se.etimo.slack.SlackRead._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.math.BigDecimal.RoundingMode
import scala.util.matching.Regex
object SlackRead {
  val excerpt_separator ="<!--excerpt-->"
  val keyFormatBase:DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd")
  val postFormatBase:DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-kk hh:mm ss")
  case class SlackSetup(token:String,
                        slackChannel:String,
                        postDirectory:String,
                        assetDirectory:String,
                        baseTitle:String,
                        startDate:DateTime,
                        blogPeriod:String,
                        client:BlockingSlackApiClient,
                        timeZone:DateTimeZone,
                        keyFormat:DateTimeFormatter,
                        postFormat:DateTimeFormatter,
                       )
  implicit val system:ActorSystem = ActorSystem("etimoslack")

  val weekdays:Map[String,Int] = Map(("monday",1),
    ("tuesday",2),
    ("wednesday",3),
    ("thursday",4),
    ("friday",5),
    ("saturday",6),
    ("sunday",7))
  val postFormatHour:DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd hh:mm")
  case class Message(date:DateTime,
                     name:String,
                     text:String,
                     blogKey:String,
                     files:Option[List[SlackFileInfo]]=None,
                     attachments:Option[List[Attachment]]=None
                    )

  private val numeric:Regex = "^[1-9]+$".r

  /**
    * Returns a method that can be used to calculate the correct grouping key for a message.
    * @param slackSetup <code>SlackSetup</code> generated from a specific config with a blogPeriodBreak setting.
    * @return <code>(date:DateTime) => String</code> Function that converts a date into a periodKey.
    */
  def getBlogKeyMethod(implicit slackSetup: SlackSetup):(DateTime) => String ={
    slackSetup.blogPeriod match {
      case "daily" => date:DateTime => date.toString(slackSetup.keyFormat)
      case x if weekdays.contains(x.toLowerCase())=> //Returns a method to find the first matching weekday
        val weekDay = weekdays(x.toLowerCase())
        var first = slackSetup.startDate
        while(first.getDayOfWeek != weekDay){
          first = first.withDurationAdded(Duration.standardDays(1),1).withTimeAtStartOfDay()
        }
        println(s"First $x is on ${first.toString(slackSetup.keyFormat)}")
        date:DateTime =>{
          if(date.getMillis <= first.getMillis) "0"
          else{
            var period = 0
            while(first.withDurationAdded(Duration.standardDays(7),period).getMillis <= date.getMillis){
              period=period+1
            }
            period.toString

          }
        }

      case x if numeric.pattern.matcher(x).matches() =>
        val period = BigDecimal(x.toInt*3600)
        date:DateTime =>  {
          val diff = (slackSetup.startDate.getMillis-date.getMillis)/1000
          (BigDecimal(diff) / period).setScale(0,RoundingMode.DOWN).toString()
        }
      case x:String =>
        throw new InstantiationError(s"Cannot bundle with period $x\n Please see README.md for compatible values.")
    }
  }

  /**
    * Blog titles are set depending on the blogPeriodBreak setting, this method sets up
    * the blog title generator function.
    * @param slackSetup The slack config object.
    * @return
    */
  def getBlogTitleGeneratorFunction(implicit slackSetup: SlackSetup):(DateTime) => String ={
    slackSetup.blogPeriod match {
      case "daily" => date:DateTime => date.toString(slackSetup.keyFormat)
      case x if weekdays.contains(x.toLowerCase())=> //Returns a method to find the first matching weekday
        date:DateTime =>{
          date.toString(slackSetup.keyFormat)+" - "+date.withDurationAdded(Duration.standardDays(7),1)
            .toString(slackSetup.keyFormat)
        }

      case x if numeric.pattern.matcher(x).matches() =>
        val period = x.toInt
        date:DateTime =>  {
          date.toString(postFormatHour)+" - "+date.withDurationAdded(Duration.standardHours(period),1).toString(postFormatHour)
        }


      case x:String =>
        throw new InstantiationError(s"Cannot bundle with period $x\n Please see README.md for compatible values.")

    }
  }
  def apply(config:String): SlackRead = new SlackRead(config)
}

class SlackRead(configFile:String) {
  implicit val setup:SlackSetup = readConfig(configFile)
  val getKey:(DateTime)=>String = getBlogKeyMethod
  val getTitle:(DateTime)=>String = getBlogTitleGeneratorFunction
  val emojiHandler:EmojiHandler = new EmojiHandler
  val emoticons = emojiHandler.getSlackEmoji(setup)

  def readConfig(configFile:String="application.conf"): SlackSetup ={

    val config = ConfigFactory.parseFile(new File(configFile))
    val token = config.getString("slackToken")
    val slackChannel = config.getString("slackChannel")
    val directory = config.getString("postDirectory")
    val baseTitle = config.getString("baseTitle")
    val blogPeriod = config.getString("blogPeriodBreak")
    val assetDirectory = config.getString("assetDirectory")
    val startDate = config.getString("startDate")
    val postDateTimeFormat = config.getString("postDateTimeFormat")
    val timeZoneId = config.getString("timeZoneId")
    val timeZone = if(timeZoneId == "default") DateTimeZone.getDefault else DateTimeZone.forID(timeZoneId)
    val blockingSlackClient = BlockingSlackApiClient(token)
    SlackSetup(token,slackChannel
      ,directory
      ,assetDirectory
      ,baseTitle
      ,keyFormatBase.withZone(timeZone).parseDateTime(startDate)
      ,blogPeriod
      ,blockingSlackClient
      ,timeZone
      ,keyFormatBase.withZone(timeZone)
      ,DateTimeFormat.forPattern(postDateTimeFormat).withZone(timeZone)
    )
  }

  private def addExcerptMarker(body: String):String = {
    val regexp = "</section".r
    val sections = regexp.findAllMatchIn(body).toList.sortWith((a,b)=> a.start<b.start)
    val build = new StringBuilder(body)
    if(sections.size > 5){
      build.insert(sections(4).start,s"\n$excerpt_separator\n").toString()
    }
    else if(sections.isEmpty){
      body
    }
    else{
      build.insert(sections.last.start,s"\n$excerpt_separator\n").toString()

    }


  }

  def writePost(dateText:String, baseDir: String, title: String, body: String): Unit = {
    val file = scala.reflect.io.File(baseDir+"/"+dateText+"-"+title.replace(" ","-")+".MARKDOWN")
    val yamlHead =
      s"""---
        |layout: post
        |title: $title
        |excerpt_separator: $excerpt_separator
        |---""".stripMargin
    val bodyWithExcerpt = addExcerptMarker(body)
    val finalPost = yamlHead+
      bodyWithExcerpt
    println(finalPost)
    file.writeAll(finalPost)

  }

  /**
    * TODO: Add interval option.
    *
    */
  def buildBlogPages():Unit = {
    val channel = setup.client.listChannels()
      .find(c => c.name.equals(setup.slackChannel)).get
    val messages =  new ListBuffer[JsValue]
    var history = setup.client
      .getChannelHistory(channel.id,oldest = Option((setup.startDate
        .getMillis/1000).toString),count = Option(500))
    messages ++= history.messages
    while(history.has_more){
      history = setup.client.getChannelHistory(channel.id,oldest = Option((messages.last \ "ts").as[String]))
      messages ++= history.messages
    }
    println(s"${messages.size} messages in ${setup.slackChannel}")
    val uidNameMap  = mutable.HashMap[String,String]()

    val betterMessages = messages.map( m => {
      println(m)
      createMessage(m,uidNameMap)
    })
    handleMessages(betterMessages,
      setup.startDate,uidNameMap.toMap
      ,setup)
  }

  /**
    *
    * @param m
    * @param uidNameMap
    * @return
    */
  private def createMessage(m:JsValue,uidNameMap:mutable.HashMap[String,String]): Message ={
    val userId = (m \ "user").as[String]
    val name = uidNameMap.getOrElse(userId,
      setup.client.getUserInfo(userId).name)
    val text = (m \ "text").as[String]
    val files = FileHandler.checkFiles(m)
    val attachments = LinkTools.getAttachment(m)
    uidNameMap.put(userId,name)
    val date = getDateForTs((m \ "ts").as[String])
    Message(date,name,emojiHandler.unicodeEmojis(text,replacementImage = Option(emoticons)),
      getKey(date),files=files,attachments)
  }

  def buildPost(messages: List[Message], uidNameMap: Map[String, String]):String = {
    val head =s"### ${messages.head.name} - ${messages.head.date.toString(
      setup.postFormat)}s\n"
    val body = messages.map(m =>
      markdownMessage(m,uidNameMap)).fold("") {(acc,m)=>s"$acc\n$m"}
    head+body
  }


  def handleMessages(betterMessages:Seq[Message], getFrom:DateTime,
                     uidNameMap:Map[String,String], config:SlackSetup):Unit = {
    //Filter by time
    //val lateMessages = betterMessages.filter(bm => bm.date.toDate.getTime > getFrom.toDate.getTime)
    val mergedMessages = MergeMessages.mergeMessages(1000*60*5,betterMessages)
    val daySeqMap = Map(mergedMessages.sortBy(m=>m.head.date.getMillis)
      .map(m=>m.head.blogKey)
      .distinct.map(dk=>
    {
      (dk,mutable.ListBuffer.empty[List[Message]])
    }) : _*)
    mergedMessages.foreach(bm => daySeqMap(bm.head.blogKey) += bm)
    daySeqMap.foreach(e => {
      val builder = mutable.StringBuilder.newBuilder
      e._2.sortBy(m=>m.head.date.getMillis)
        .foreach(m => builder.append("\n<section class=\"message\" markdown=\"1\">\n")
          .append(buildPost(m,uidNameMap))
          .append("\n").append("</section>"))
      writePost(
        e._2.head.head.date.toString(config.keyFormat) //Date text in JekyllRB post name
        ,config.postDirectory
        ,s"${config.baseTitle} ${getTitle(e._2.head.head.date)}",builder.toString())
    })
  }

  def buildImageElement(f: SlackFileInfo): String = {
    val url = FileHandler.getDownloadedThumbUrl(f)
    val fileUrl = FileHandler.getDownloadedFileUrl(f)
    s"""
       |<div class="imageblock">
       |<a href="$fileUrl">
       |<img alt="${f.name}" src="$url"/>
       |</a></div>
       |
     """.stripMargin
  }

  def buildPreviewElement(f: SlackFileInfo): String = {
    val fileUrl = FileHandler.getDownloadedFileUrl(f)
    s"""
       |<div class="preview">
       |<div class="text">
       |${f.previewHighlight.getOrElse(f.preview)}
       |</div>
       |<a href="$fileUrl">${f.name}</a>
       |</div>
       |""".stripMargin
  }

  def buildDownloadElement(f: SlackFileInfo):String = {
    val fileUrl = FileHandler.getDownloadedFileUrl(f)
    s"""
       |<div class="fileblock">
       |<div class="text">
       |</div>
       |<a href="$fileUrl">${f.name}</a>
       |</div>
       |""".stripMargin
  }

  /**
    *
    * @param message
    * @return
    */
  def createFileMarkdown(message: Message): String = {
    message.files.get.map(f => {
      if(!FileHandler.checkFileDownloaded(f)){
        //Get file if not downloaded.
        val fileBytes = FileHandler.downloadFile(f)
        FileHandler.writeFile(fileBytes,FileHandler.getFilePath(f))
      }
      if(f.mimeType.getOrElse("").startsWith("image")){
        FileHandler.generatePublicUrl(f)
        if(!FileHandler.checkThumbDownload(f)){
          val thumb = FileHandler.downloadThumb(f)
          FileHandler.writeFile(thumb,FileHandler.getThumbPath(f))
        }
        f.title.getOrElse("")+"\n"+buildImageElement(f)
      }
      else if(f.previewHighlight.isDefined||f.preview.isDefined){
        buildPreviewElement(f)
      }
      else{
        buildDownloadElement(f)
      }
    }).mkString("\n")

  }

  def markdownMessage(message: Message, uidmap:
  //<section class="message" markdown="1"> - should messages be wrapped?
  Map[String,String]): String = {
    val base = checkMessageForAt(message.text,uidmap)
    if(message.files.isDefined){
      createFileMarkdown(message)
    }
    else if(message.attachments.isDefined){
      base.replace("<"," [").replace(">","]")+ message.attachments.get.
        map(a=>LinkTools.buildAttachmentMarkdown(a)).mkString("\n")
    }
    else
      base.replace("<"," [").replace(">","]")
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
    * @param message <code>Message</code> message to check text for @ calls
    * @param uidToNameMap <code>Map<code> of UID to full username.
    * @return text from message with any @ calls replaced with usernames.
    */
  def checkMessageForAt(message:String,uidToNameMap:Map[String,String]) :String = {
    var returnMessage = message
    message.split("<@").foreach(s => {
      val key = s.split(">")(0)
      val name = uidToNameMap.get(key)
      returnMessage = returnMessage.replace(s"<@$key>","@"+name.getOrElse("Unkown"))
    })
    returnMessage = checkMessageForLink(returnMessage)
    returnMessage = MarkdownTweaker.checkMarkdownCodeBlock(returnMessage)
    returnMessage
  }


}