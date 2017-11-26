package se.etimo.slack

import java.io.File
import java.text.SimpleDateFormat

import com.typesafe.config.ConfigFactory
import slack.api.BlockingSlackApiClient
import akka.actor.ActorSystem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable

object Slackread {
  case class SlackConfig( token:String, slackChannel:String,
                     directory:String, baseTitle:String, startDate:String,client:BlockingSlackApiClient)

  implicit val system = ActorSystem("etimoslack")
  val keyformat = DateTimeFormat.forPattern("YYYY-MM-dd")
  val postFormat = DateTimeFormat.forPattern("YYYY-MM-dd hh:mm ss")

  case class Message(val date:DateTime,val name:String,val text:String,val dayKey:String)
  def readConfig(configFile:String="application.conf"): SlackConfig ={
    val config = ConfigFactory.parseFile(new File(configFile))
    val token = config.getString("slackToken")
    val slackChannel = config.getString("slackChannel")
    val directory = config.getString("postDirectory")
    val baseTitle = config.getString("baseTitle")
    val startDate = config.getString("startDate")

    val blockingSlackClient = BlockingSlackApiClient(token)
    return SlackConfig(token,slackChannel
      ,directory,baseTitle
      ,startDate,blockingSlackClient)

  }

  def writePost(dateText:String,baseDir: String, title: String, body: String): Unit = {
    val file = scala.reflect.io.File(baseDir+"/"+dateText+"-"+title.replace(" ","-")+".MARKDOWN")
    val yamlHead ="---\nlayout: post\ntitle: "+title+"\n---\n"
    val finalPost = yamlHead+
    body
    println(finalPost)
    file.writeAll(finalPost)

  }
  def buildBlogPages(configFile:String ="application.conf") = {
    val config = readConfig(configFile)
    val getFrom = keyformat.parseDateTime(config.startDate);
    val channel = config.client.listChannels()
      .find(c => c.name.equals(config.slackChannel)).get
    val messages =  config.client
      .getChannelHistory(channel.id).messages
    val uidNameMap  = mutable.HashMap[String,String]()
    val betterMessages = messages.map(f = m => {
      val userId = (m \ "user").as[String]
      val name = uidNameMap.get(userId).getOrElse(
        config.client.getUserInfo(userId).name)
      val text = (m \ "text").as[String]
      uidNameMap.put(userId,name)
      val date = getDateForTs((m \ "ts").as[String])
      Message(date,name,text,date.toString(keyformat))
    })
    //Filter by time
    val lateMessages = betterMessages.filter(bm => bm.date.toDate.getTime > getFrom.toDate.getTime)
    val daySeqMap = Map(lateMessages.sortBy(m=>m.date.toDate.getTime)
      .map(m=>m.dayKey)
      .distinct.map(dk=>
    {
      (dk,mutable.ListBuffer.empty[Message])
    }) : _*)
    lateMessages.foreach(bm => daySeqMap.get(bm.dayKey).get += bm)
    daySeqMap.foreach(e => {
      val builder = mutable.StringBuilder.newBuilder
      e._2.sortBy(m=>m.date.toDate.getTime)
        .foreach(m => builder.append(markdownMessage(m,uidNameMap)))
      writePost(e._1,config.directory
        ,s"${config.baseTitle} ${e._1}",builder.toString())
    })
  }


  def markdownMessage(message: Message,uidmap:
  mutable.HashMap[String,String]): String = {
    val builder = new StringBuilder();

    builder.append("### ").append(message.name).append(" - ")
      .append(message.date.toString(postFormat)).append("s")
      .append("\n")
    builder.append(
      checkMessageForAt(message.text,uidmap)
      .replace("<"," [").replace(">","]")).append("\n")
    return builder.toString()
  }
  def getDateForTs(ts:String): DateTime ={
    return new DateTime(ts.substring(0,ts.indexOf(".")).toLong*1000)
  }

  def checkMessageForLink(message:String) :String = {
    var returnMessage = message
    message.split("<").foreach(s => {
      val url = s.split(">")(0)
      returnMessage = returnMessage.replace(s"<$url>",s"[$url]($url)")

    })
    return returnMessage
  }
  def checkMessageForAt(message:String,uidToNameMap:mutable.HashMap[String,String]) :String = {
    var returnMessage = message
    message.split("<@").foreach(s => {
      val key = s.split(">")(0)
      val name = uidToNameMap.get(key)
      returnMessage = returnMessage.replace(s"<@$key>","@"+name.getOrElse("Unkown"))

    })
    return checkMessageForLink(returnMessage);
  }

}