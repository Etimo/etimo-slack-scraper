package se.etimo.slack

import org.joda.time.DateTime, se.etimo.slack.SlackRead.Message

import org.scalatest.FunSuite

class MergeMessageTest extends FunSuite {
  val baseDate = SlackRead.postFormat.parseDateTime("2017-12-17 10:10 30")
  val messages = Seq(
    Message(baseDate,"Erik","I","xx"),
    Message(baseDate.withDurationAdded(5,1),"Erik","should","xx"),
    Message(baseDate.withDurationAdded(10,1),"Erik","merge","xx"),
    Message(baseDate.withDurationAdded(15,1),"Erik","!","xx"),
    Message(baseDate.withDurationAdded(60,1),"Erik","I shouldn't merge!", "xx"),
    Message(baseDate.withDurationAdded(60,1),"Johan","I shouldn't merge!", "xx"),
    Message(baseDate.withDurationAdded(65,1),"Johan","With Eriks messages!", "xx"),
  )
  def testMergeTest(): Unit ={
    MergeMessages.mergeMessages(10,messages)
  }

}