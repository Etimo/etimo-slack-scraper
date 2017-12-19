package se.etimo.slack


import org.joda.time.DateTime
import scala.collection.mutable.ListBuffer
import se.etimo.slack.SlackRead.Message

object MergeMessages{

  private case class MergeMessage(firstTime:DateTime, lastTime:DateTime, name:String, text:String)
  private val startTime:DateTime = new DateTime(0);
 private val emptyMessage=MergeMessage(startTime,startTime,"NONAME","NOTEXT")

  def mergeMessages(timeDiff:Integer,messages:Seq[Message]): Seq[Message] = {
    val mergedMessages = ListBuffer.empty[MergeMessage]
    var bufferMessage = emptyMessage
      messages.sortBy(m => m.date.toDate.getTime).foreach(m => {
        if(checkMerge(timeDiff,bufferMessage,m)){
          bufferMessage=merge(bufferMessage,m)
        }
        else{
         if(bufferMessage != emptyMessage) {mergedMessages += bufferMessage}
          bufferMessage= MergeMessage(m.date,m.date,m.name,m.text)
        }
    })
    mergedMessages.map(mm => Message(mm.firstTime,mm.name,mm.text,"xx"))
  }
  def checkMerge(timeDiff:Integer,messageOne:MergeMessage,messageTwo:Message):Boolean = {
      messageOne.name.equals(messageTwo.name) &&
      messageTwo.date.toDate.getTime - messageOne.lastTime.toDate.getTime() < timeDiff
  }
  private def merge(mergeMessage:MergeMessage,message:Message): MergeMessage ={
    MergeMessage(mergeMessage.firstTime,
      message.date,
      message.name,
      mergeMessage.text+"\n  "+message.text,
      )
  }


}
