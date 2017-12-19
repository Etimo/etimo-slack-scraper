package se.etimo.slack

import org.joda.time.DateTime, se.etimo.slack.SlackRead.Message

import org.scalatest._

class MergeMessagesTest extends FlatSpec with Matchers {
  val baseDate = SlackRead.postFormat.parseDateTime("2017-12-17 10:10 30")
  val messages = List(
    Message(baseDate,"Erik","I","xx"),
    Message(baseDate.withDurationAdded(5,1),"Erik","should","xx"),
    Message(baseDate.withDurationAdded(10,1),"Erik","merge","xx"),
    Message(baseDate.withDurationAdded(15,1),"Erik","!","xx"),
    Message(baseDate.withDurationAdded(60,1),"Erik","I shouldn't merge!", "xx"),
    Message(baseDate.withDurationAdded(60,1),"Johan","I shouldn't merge!", "xx"),
    Message(baseDate.withDurationAdded(65,1),"Johan","With Eriks messages!", "xx"),
  )
  val mergedMessages = List(
    Message(baseDate,"Erik","I\n  should\n  merge\n  !","xx"),
    Message(baseDate.withDurationAdded(60,1),"Erik","I shouldn't merge!", "xx"),
    Message(baseDate.withDurationAdded(60,1),"Johan","I shouldn't merge!\n  With Eriks messages!", "xx"),
  )

    val merged = MergeMessages.mergeMessages(10,messages)
    "Messages close in tim by a single person" should "merge" in {
    merged(0) should be (mergedMessages(0))
    merged(1) should be (mergedMessages(1))
    merged(2) should be (mergedMessages(2))
    }

}