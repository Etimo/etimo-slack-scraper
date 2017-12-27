package se.etimo.slack

import com.typesafe.config.ConfigFactory

import scala.util.matching.Regex;

/**
  * Slack accepts and outputs markdown in ways that aren't processed well by
  * JekyllRB with Rouge and Kramdown. This class contains methods to analyse and tweak the codeblocks.
  * Allowed syntaxes can be modified to "syntaxrouge.conf" in main/resources
  */
object MarkdownTweaker {
  private case class HighLightBlock(start:Int,stop:Int,text:String)
    private val syntaxHighLight = ConfigFactory.load("syntaxrouge").getStringList("syntaxes")
  private val dumbBlockMatcher = "```".r.unanchored
    private val blockMatcher = "```(\\w+)".r.unanchored
    private val blockMatchStart = ".```".r.unanchored

    def checkMarkdownCodeBlock(text:String): String ={
        val matches = dumbBlockMatcher.findAllMatchIn(text)
      val blocks = matches.foldLeft((List[HighLightBlock](),Option.empty[Regex.Match]))((o,b) =>{
        if(o._2.isEmpty)(o._1,Option(b))
        else{
          val start = o._2.get.start
          val stop = b.start+3
          (HighLightBlock(start,stop,text.substring(start,stop)) :: o._1,Option.empty[Regex.Match])
        }
      })._1
      val fixedBlocks = fixBlocks(blocks,text)
      mergeNewBlocks(fixedBlocks,text)
    }

  /**
    * Merge blocks with fixed syntax with old text in a deterministic manner.
    * @param blocks The fixed blocks
    * @param text The original text containing blocks
    * @return String where text defining old blocks is replaced with new text.
    */
  private def mergeNewBlocks(blocks:List[HighLightBlock],text:String):String = {
    val builder =new StringBuilder()
    if(blocks.isEmpty){
     return text
    }
    var offset=0
    blocks.foreach(b => {
      builder.append(text.substring(offset,b.start)).append(b.text)
     offset=b.stop
    })
    builder.append(text.substring(blocks.last.stop))
    builder.toString()
  }

  /**
  * Add line breaks where needed including checking for supported syntaxes following: ```.
  * All modifications are done by creating HighLightBlocks with modified text.
  * Original text is used for checks outside of code block Original text is used for checks outside of code block.
  */
  private def fixBlocks(blocks:List[HighLightBlock],text:String):List[HighLightBlock]={
  blocks.sortWith((a,b)=>a.start < b.start)
          .map( b=>{
            val build =new StringBuilder(b.text)
            val blockStart = checkBlockStart(b)
           if(blockStart.isDefined)
            HighLightBlock(b.start,b.stop,build.insert(3+blockStart.get.length,'\n').toString()) else b
        })
        .map(b=>{
          val build =new StringBuilder(b.text)
          if(checkBlockEnd(b))
            HighLightBlock(b.start,b.stop,build.insert(b.text.length-3,'\n').toString()) else b
        })
        .map(b => {
        if (b.start > 0 && text.charAt(b.start - 1) != '\n')
          HighLightBlock(b.start, b.stop, "\n" + b.text) else b
      }).map(b => {
       if(b.stop<text.length-1 &&
         text.charAt(b.stop)!='\n')
         HighLightBlock(b.start,b.stop,b.text+"\n") else b
      })
  }
  private def checkBlockStart(highLightBlock: HighLightBlock): Option[String] ={
    val matched = blockMatcher.findFirstMatchIn(highLightBlock.text)
    if(matched.isEmpty){
      return Option.empty
    }
    val syntax = matched.get.group(1)
    if(syntaxHighLight.contains(syntax)){
      return Option(syntax)
    }
    Option("")
  }
  private def checkBlockEnd(highLightBlock: HighLightBlock): Boolean ={
    highLightBlock.text.charAt(highLightBlock.text.length-4) != '\n'
  }
}
