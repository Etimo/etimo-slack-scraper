package se.etimo.slack

import java.text.SimpleDateFormat

import com.typesafe.config.ConfigFactory
import slack.api.BlockingSlackApiClient
import akka.actor.ActorSystem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable
import scala.reflect.io.File

object Slackread {

  implicit val system = ActorSystem("etimoslack")
  val keyformat = DateTimeFormat.forPattern("YYYY-MM-dd")
  val postFormat = DateTimeFormat.forPattern("YYYY-MM-dd hh:mm ss")
  val token = ConfigFactory.load().getString("slackToken")
  val slackChannel = ConfigFactory.load().getString("slackChannel")
  val directory = ConfigFactory.load().getString("postDirectory")
  val baseTitle = ConfigFactory.load().getString("baseTitle")
  val startDate = ConfigFactory.load().getString("startDate")
  val blockingSlackClient = BlockingSlackApiClient(token)

  case class Message(val date:DateTime,val name:String,val text:String,val dayKey:String)

  def writePost(dateText:String,baseDir: String, title: String, body: String): Unit = {
    val file = File(baseDir+"/"+dateText+"-"+title.replace(" ","-")+".MARKDOWN")
    val yamlHead ="---\nlayout: post\ntitle: "+title+"\n---\n"
    val finalPost = yamlHead+
    body
    println(finalPost)
    file.writeAll(finalPost)

  }

  def buildBlogPages = {
    val getFrom = keyformat.parseDateTime(startDate);
    val channel = blockingSlackClient.listChannels()
      .find(c => c.name.equals(slackChannel)).get
    val messages =  blockingSlackClient
      .getChannelHistory(channel.id).messages
    val uidNameMap  = mutable.HashMap[String,String]()
    val betterMessages = messages.map(f = m => {
      val userId = (m \ "user").as[String]
      val name = uidNameMap.get(userId).getOrElse(
        blockingSlackClient.getUserInfo(userId).name)
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
      writePost(e._1,directory
        ,s"$baseTitle "+e._1,builder.toString())
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