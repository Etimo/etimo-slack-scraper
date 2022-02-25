package se.etimo.slack

import java.io.File
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.conversations.{ConversationsHistoryRequest, ConversationsListRequest, ConversationsRepliesRequest}
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.model.Message
import org.joda.time.{DateTime, DateTimeZone, Duration}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import se.etimo.slack.reimplemented.FileHandler.SlackFileInfo
import se.etimo.slack.LinkTools.Attachment
import se.etimo.slack.ReactionHandler.Reaction
import se.etimo.slack.SlackRead._
import se.etimo.slack.reimplemented.FileHandler

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.math.BigDecimal.RoundingMode
import scala.util.matching.Regex
import collection.JavaConverters._
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
                        timeZone:DateTimeZone,
                        keyFormat:DateTimeFormatter,
                        postFormat:DateTimeFormatter,
                        methodsClient: MethodsClient
                       )
  implicit val system:ActorSystem = ActorSystem("etimoslack")

  val weekdays:Map[String,Int] =
    Map(("monday",1),
      ("tuesday",2),
      ("wednesday",3),
      ("thursday",4),
      ("friday",5),
      ("saturday",6),
      ("sunday",7))
  val postFormatHour:DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd hh:mm")

  case class BlogMessage(date:DateTime,
                         name:String,
                         text:String,
                         blogKey:String,
                         files:Option[List[SlackFileInfo]]=None,
                         reactions:Option[List[Reaction]]=None,
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
  implicit val reactionHandler = new ReactionHandler(emojiHandler,Option(emoticons))

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
    val methodsClient = com.slack.api.Slack.getInstance().methods(token);
    SlackSetup(token,slackChannel
      ,directory
      ,assetDirectory
      ,baseTitle
      ,keyFormatBase.withZone(timeZone)
        .parseDateTime(startDate)
      ,blogPeriod
      ,timeZone
      ,keyFormatBase.withZone(timeZone)
      ,DateTimeFormat.forPattern(postDateTimeFormat).withZone(timeZone),
      methodsClient
    )
  }


  /**
    * TODO: Add interval option.
    *
    */
  def buildBlogPages():Unit = {

    val messages = getMessages()
    println(s"${messages.size} messages in ${setup.slackChannel}")
    val uidNameMap  = mutable.HashMap[String,String]()
    val betterMessages = messages.flatMap( m => {
      println(m)
      m.map(mess => createMessage(mess,uidNameMap))
    })
    processMessagesForBlogBuild(
      betterMessages,
      setup.startDate,uidNameMap.toMap
      ,setup)
  }

  private def getMessages()={

    val channels = setup.methodsClient.conversationsList(
      ConversationsListRequest.builder()
        .build()
    )
    val channel = channels.getChannels().stream()
      .filter(c => c.getName().equals(setup.slackChannel)).findFirst()
      .orElseThrow(() => new RuntimeException("No channel matching specified name found"))
    println("Found channel id: "+channel.getId())
    val messages =  new ListBuffer[com.slack.api.model.Message]
    val epochStart = (setup.startDate.getMillis/1000)+"";
    var history = setup.methodsClient
      .conversationsHistory(
        ConversationsHistoryRequest.builder().channel(channel.getId())
          .oldest(epochStart)
          .limit(500).build())

    messages ++= history.getMessages().asScala


    while(history.isHasMore){
      Thread.sleep(2000) //Avoid triggering API limits
      var history = setup.methodsClient
        .conversationsHistory(
          ConversationsHistoryRequest.builder().channel(channel.getId())
            .oldest(messages.last.getTs()).limit(500).build())
      messages ++= history.getMessages().asScala
    }
    val threads = messages.map(m => getNestedMessages(m,channel.getId))
    threads;
  }

  private def getNestedMessages(message: Message, channel:String): mutable.Buffer[Message]={
    if(message.getReplyCount == 0){
      return mutable.Buffer()
    }

    val messages =  setup.methodsClient.conversationsReplies(
      ConversationsRepliesRequest.builder()
        .channel(channel)
        .inclusive(true)
        .ts(message.getTs)
        .build()).getMessages
      .asScala
    messages

  }
  /**
    * Matches userIds with names of users, handles emojis.
    * @param m
    * @param uidNameMap
    * @return
    */
  private def createMessage(m:com.slack.api.model.Message,uidNameMap:mutable.HashMap[String,String]): BlogMessage ={
    val userId = m.getUser
    val name = uidNameMap.getOrElse(userId,getName(userId,setup,300)
    )
    val text = m.getText
    val files = FileHandler.checkFiles(m)
    val attachments = Option(m.getAttachments().asScala).map(a => a.map(
      a => Attachment(a.getTitle(), a.getTitleLink, Option(a.getText), Option(a.getImageUrl), Option(a.getThumbUrl))
    ).toList
    )
    val reactions = reactionHandler.getReactions(m)
    uidNameMap.put(userId,name)
    val date = getDateForTs(m.getTs())
    BlogMessage(date,name,emojiHandler.unicodeEmojis(text,replacementImage = Option(emoticons)),
      getKey(date),files,reactions,attachments)
  }

  def buildPost(messages: List[BlogMessage], uidNameMap: Map[String, String]) :String = {
    val head =s"### ${messages.head.name} - ${messages.head.date.toString(
      setup.postFormat)}s\n"
    val body =
      messages.map(m =>
        MarkdownGenerator.markdownMessage(setup, m, uidNameMap)
          .concat(reactionHandler.buildReactionBlock(m.reactions)(uidNameMap))
      ).fold("") {(acc,m)=>s"$acc\n$m"}
    head+body
  }
  def getName(userId:String,setup: SlackSetup,rateLimit:Int): String ={
    Thread.sleep(rateLimit)
    val nameResponse = setup.methodsClient.usersInfo(UsersInfoRequest.builder().user(userId).build())
    val name = Option(nameResponse).filter(m => m.isOk).map(m => m.getUser()).map(m=>m.getName()).getOrElse(userId)
    println("Fetched name "+userId+" "+name)
    name
  }

  def getDateForTs(ts:String): DateTime ={
    new DateTime(ts.substring(0,ts.indexOf(".")).toLong*1000)
  }

  def processMessagesForBlogBuild(betterMessages:Seq[BlogMessage], getFrom:DateTime,
                                  uidNameMap:Map[String,String], config:SlackSetup):Unit = {
    val mergedMessages = MergeMessages.mergeMessages(1000*60*5,betterMessages) //Combines any messages sufficiently close in time, by the same person.
    val daySeqMap = Map(mergedMessages.sortBy(m=>m.head.date.getMillis)
      .map(m=>m.head.blogKey)
      .distinct.map(dk=>
    {
      (dk,mutable.ListBuffer.empty[List[BlogMessage]])
    }) : _*)
    mergedMessages.foreach(bm => daySeqMap(bm.head.blogKey) += bm) //Messages with the same blogkey will be exported into the same post. Here they are grouped in lists.
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

  private def addExcerptMarker(body: String):String = {
    val regexp = "</section".r
    val sections = regexp.findAllMatchIn(body).toList.sortWith((a,b)=> a.start<b.start)
    val build = new StringBuilder(body)
    if(sections.size > 25){
      build.insert(sections(24).start,s"\n$excerpt_separator\n").toString()
    }
    else if(sections.isEmpty){
      body
    }
    else{
      build.insert(sections.last.start,s"\n$excerpt_separator\n").toString()

    }


  }
}