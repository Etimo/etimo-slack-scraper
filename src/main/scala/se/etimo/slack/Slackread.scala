package se.etimo.slack

import com.typesafe.config.ConfigFactory , slack.api.BlockingSlackApiClient, akka.actor.ActorSystem

 object Slackread {
   implicit val system = ActorSystem("etimoslack")
   val token = ConfigFactory.load().getString("slacktoken")
   val blockingSlackClient = BlockingSlackApiClient(token);
   blockingSlackClient.listChannels().foreach(c => print(c.name))

   def listChannels = {


   }

}