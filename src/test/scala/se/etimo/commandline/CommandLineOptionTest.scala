package se.etimo.commandline

import org.scalatest.FunSuite

class CommandLineOptionTest extends FunSuite {
  var triggerTest = false;
  val testOption = CommandLineOption(
    List("-t","-test")
    ,2
    ,"This is a fake helpmessage"
    ,Option(list => {triggerTest = true})
    ,Option(list => true)
  )()
  val goodArgs = Array("-t","consumeme","consumemetoo")
  val goodArgsTwo = Array("-test","consumeme","consumemetoo")
  val consumeFields = goodArgs.filter(s=>testOption.flags.contains(s)==false).toList
  val badArgs = Array("-te","consumeme","consumemetoo")

  assert(!testOption.triggered,"Default value should be untriggered")
  val goodOptOne = testOption.consumeOptions(goodArgs)
  val goodOptTwo = testOption.consumeOptions(goodArgsTwo)
  val badOpt = testOption.consumeOptions(badArgs)
  assert(goodOptOne.triggered,"Arguments should have triggered.")
  assert(goodOptTwo.triggered,"Arguments should have triggered.")
  assert(!badOpt.triggered,"Arguments should NOT have triggered.")
  assertResult(consumeFields)(goodOptOne.readFields)
  assertResult(consumeFields)(goodOptTwo.readFields)
  goodOptOne.runAction()
  assert(triggerTest,"Action should have flipped the trigger")

}
