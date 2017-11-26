package se.etimo.commandline;

import scala.collection.mutable.ListBuffer
import scala.Array
object  CommandLineOption {

  def apply(flags: List[String], fieldsAfterFlag: Int,
            helpMessage: String,
            action: Option[List[String] => Unit],
            checkMethod: Option[List[String] => Boolean])
           (triggered: Boolean = false, readFields: List[String] = List[String]()): CommandLineOption =
    new CommandLineOption(flags, fieldsAfterFlag, helpMessage, action,checkMethod)(triggered, readFields)

}

/**
  * Utility class to parse input from command-line.
  * Will if one of their flags is found store the next N number
  * of fields for further processing.
  */
class CommandLineOption(val flags:List[String], val fieldsAfterFlag:Int,
                        val helpMessage:String,
                        action:Option[List[String] => Unit],
                        checkMethod:Option[List[String] => Boolean])
                       (val triggered:Boolean=false,
                        val readFields:List[String]=List[String]()) {
  def runAction(): Unit ={
    action.foreach(x=>x(readFields))
  }
  def checkInput(): Boolean ={
    def allTrue(x:List[String]) ={ true }
    checkMethod.getOrElse(allTrue _)(readFields)
  }

  /**
    * Looks for any of it's defined flags and
    * then consumes it's defined number of fields.
    * Returns a new CommandLineOption with new values for triggered
    * and fieldsRead
    * @param arguments An argument array from the call to the program.
    * @return
    */
  def consumeOptions(arguments:Array[String]):CommandLineOption = {
    var max = fieldsAfterFlag
    var buffer = ListBuffer.empty[String]
    var readBuffer = ListBuffer.empty[String]
    var tmpTriggered = false;
    arguments.foreach(f => {
      if (tmpTriggered && max > 0) {
        readBuffer += f
        max = max - 1
      }
      else buffer + f
      if (flags.contains(f)) {
        tmpTriggered = true;
      }
    })
    CommandLineOption(flags,fieldsAfterFlag,helpMessage,action,checkMethod)(tmpTriggered,readBuffer.toList)

  }
}
