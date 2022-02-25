package se.etimo.slack

import org.joda.time.DateTime
import org.scalatest._
import se.etimo.slack.SlackRead.SlackSetup,se.etimo.slack.SlackRead.BlogMessage

class SlackReadTest extends FlatSpec with Matchers {
  val baseDate = SlackRead.postFormatBase.parseDateTime("2017-12-17 10:10 30")
  val messages = List(
    BlogMessage(baseDate,"Erik","I","xx"),
    BlogMessage(baseDate.withDurationAdded(5,1),"Erik","should","xx"),
    BlogMessage(baseDate.withDurationAdded(10,1),"Erik","merge","xx"),
    BlogMessage(baseDate.withDurationAdded(15,1),"Erik","!","xx"),
    BlogMessage(baseDate.withDurationAdded(60,1),"Erik","I shouldn't merge!", "xx"),
    BlogMessage(baseDate.withDurationAdded(60,1),"Johan","I shouldn't merge!", "xx"),
    BlogMessage(baseDate.withDurationAdded(65,1),"Johan","With Eriks messages!", "xx"),
  )
  val mergedMessages = List(
    BlogMessage(baseDate,"Erik","I\n  should\n  merge\n  !","xx"),
    BlogMessage(baseDate.withDurationAdded(60,1),"Erik","I shouldn't merge!", "xx"),
    BlogMessage(baseDate.withDurationAdded(60,1),"Johan","I shouldn't merge!\n  With Eriks messages!", "xx"),
  )
  SlackSetup("No"
    ,"Token"
    ,"directory either"
    ,"no asset directory either"
    ,"channel"
    ,DateTime.now()
    ,"Monday"
    ,null,null,null,null)
  "Making views" should "merge" in {

  }
  //SlackRead()

}
