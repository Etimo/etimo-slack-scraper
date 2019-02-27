package se.etimo.slack;
import org.joda.time.{DateTime, DateTimeZone, Duration}
import se.etimo.slack.SlackRead.SlackSetup
object StartDateCalculator{
        val PERIOD_ENDING = "P";
        def calculateStartDate(dateSetting:String,blogBreak:Int):DateTime = {
          dateSetting.endsWith(PERIOD_ENDING)
          null
          }
        }


