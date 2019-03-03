package se.etimo.slack

import se.etimo.slack.FileHandler.SlackFileInfo
import se.etimo.slack.SlackRead.{Message, SlackSetup}

object MarkdownGenerator {

  private def buildImageElement(f: SlackFileInfo,minThumbSize:Int=800): String = {
    val fileUrl = FileHandler.getDownloadedFileUrl(f)
    val url = if (f.thumb_info.size >= minThumbSize) FileHandler.getDownloadedThumbUrl(f) else fileUrl
    s"""
       |<div class="imageblock">
       |<a href="$fileUrl">
       |<img alt="${f.name}" src="$url"/>
       |</a></div>
       |
     """.stripMargin
  }

  private def buildPreviewElement(f: SlackFileInfo): String = {
    val fileUrl = FileHandler.getDownloadedFileUrl(f)
    s"""
       |<div class="preview">
       |<div class="text">
       |${f.previewHighlight.getOrElse(f.preview)}
       |</div>
       |<a href="$fileUrl">${f.name}</a>
       |</div>
       |""".stripMargin
  }

 private def buildDownloadElement(f: SlackFileInfo):String = {
    val fileUrl = FileHandler.getDownloadedFileUrl(f)
    s"""
       |<div class="fileblock">
       |<div class="text">
       |</div>
       |<a href="$fileUrl">${f.name}</a>
       |</div>
       |""".stripMargin
  }

  /**
    *
    * @param message
    * @return
    */
   def createFileMarkdown(message: Message)(implicit slackSetup:SlackSetup): String = {
    message.files.get.map(f => {
      if(!FileHandler.checkFileDownloaded(f)){
        //Get file if not downloaded.
        val fileBytes = FileHandler.downloadFile(f)
        FileHandler.writeFile(fileBytes,FileHandler.getFilePath(f))
      }
      if(f.mimeType.getOrElse("").startsWith("image")){
        FileHandler.generatePublicUrl(f)
        if(!FileHandler.checkThumbDownload(f)){
          val thumb = FileHandler.downloadThumb(f)
          FileHandler.writeFile(thumb,FileHandler.getThumbPath(f))
        }
        buildImageElement(f)
      }
      else if(f.previewHighlight.isDefined||f.preview.isDefined){
        buildPreviewElement(f)
      }
      else{
        buildDownloadElement(f)
      }
    }).mkString("\n")

  }

  /** Todo: Break all markdown related methods out into MarkDownTweak for
    * increased testability.
    * Generates markdown from a preprocessed slackmessage.
    * @param inMessage Message to create markdown for.
    * @param uidmap Map of userIds to usernames
    * @return
    */
  def markdownMessage(implicit slackSetup: SlackSetup, inMessage: Message, uidmap:
  Map[String,String]): String = {
    val base = checkMessageForAt(inMessage.text,uidmap)
    if(inMessage.files.isDefined){
      createFileMarkdown(inMessage)
    }
    else if(inMessage.attachments.isDefined){
      base.replace("<"," [").replace(">","]")+ inMessage.attachments.get.
        map(a=>LinkTools.buildAttachmentMarkdown(a)).mkString("\n")
    }
    else
      base.replace("<"," [").replace(">","]")
  }

  def checkMessageForLink(message:String) :String = {
    var returnMessage = message
    message.split("<").foreach(s => {
      val url = s.split(">")(0)
      returnMessage = returnMessage.replace(s"<$url>",s"[$url]($url)")

    })
    returnMessage
  }

  /**
    * Check a message for an @ call to another person and
    * replace it with a markdown compatible string of the persons name.
    * @param message <code>Message</code> message to check text for @ calls
    * @param uidToNameMap <code>Map<code> of UID to full username.
    * @return text from message with any @ calls replaced with usernames.
    */
  def checkMessageForAt(message:String,uidToNameMap:Map[String,String]) :String = {
    var returnMessage = message
    message.split("<@").foreach(s => {
      val key = s.split(">")(0)
      val name = uidToNameMap.get(key)
      returnMessage = returnMessage.replace(s"<@$key>","@"+name.getOrElse("Unkown"))
    })
    returnMessage = checkMessageForLink(returnMessage)
    returnMessage = MarkdownTweaker.checkMarkdownCodeBlock(returnMessage)
    returnMessage
  }


}
