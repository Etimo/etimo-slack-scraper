package se.etimo.slack


import org.joda.time.DateTime
import scala.collection.mutable.ListBuffer
import se.etimo.slack.SlackRead.Message

object MergeMessages{

  private case class MergeMessage(messages:List[Message])
  private val startTime:DateTime = new DateTime(0);

  def mergeMessages(timeDiff:Integer,messages:Seq[Message]): List[List[Message]] = {
    val mergedMessages = ListBuffer.empty[List[Message]]
    var bufferMessages  = ListBuffer[Message]()
      messages.sortBy(m => m.date.toDate.getTime).foreach(m => {
        if(checkMerge(timeDiff,bufferMessages.toList,m)){
          bufferMessages+=m
        }
        else{
          mergedMessages+=bufferMessages.toList
          bufferMessages.clear()
          bufferMessages+=m
        }
    })
    if(!mergedMessages.contains(bufferMessages.toList))mergedMessages+=bufferMessages.toList
    return mergedMessages.toList
  }
  def checkMerge(timeDiff:Integer,bufferMessages:List[Message],messageTwo:Message):Boolean = {
    if(bufferMessages.isEmpty) return true
     val messageOne = bufferMessages.last
      messageOne.name.equals(messageTwo.name) &&
      messageTwo.date.toDate.getTime - messageOne.date.toDate.getTime() < timeDiff
  }


}
