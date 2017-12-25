import se.etimo.commandline.CommandLineOption
import se.etimo.slack.SlackRead

import scala.reflect.io.File
object Main extends App {

  val configOpt = CommandLineOption(
    List[String]("-c","--config")
    ,1
    ,"Specify the location of the configuration file to use."
    ,Option(list =>{SlackRead(list.head).buildBlogPages()})
    ,Option(list =>
      list.size > 0 && File(list(0)).exists))()

  val helpOpt = CommandLineOption(List[String](
    "-h","--help")
    ,1
    ,"Specify the location of the configuration file to use."
    ,Option(list =>{
      allOpts.foreach(o => {
          println(s"${o.flags.mkString(", ")} ${o.helpMessage}")
      })
    }), Option((list:List[String]) =>
    true))()

  val allOpts:List[CommandLineOption] = List(helpOpt,configOpt)
  val checkedOpts = allOpts.map(o => o.consumeOptions(args))
  checkedOpts.filter(o => o.triggered && o.checkInput()).foreach(o=>o.runAction())
  val input = configOpt.consumeOptions(args)
  System.exit(0)
}
