package se.etimo.slack

import play.api.libs.json._

import scalaj.http.{Http, HttpOptions}
import se.etimo.slack.SlackRead.SlackSetup

import scala.language.implicitConversions
import scala.reflect.io.File

object FileHandler {
  private val bufferByte =16000
  val blogPrefix = "blogAssets"
  def writeFile(bytes: Option[Array[Byte]], file: File):Unit = {
    bytes.foreach(ba=>{
      file.parent.createDirectory(true)
      val out = file.bufferedOutput(false)
      out.write(ba)
      out.close()
    })
  }


  val FILE_TYPE = "file_share"
  case class SlackFileInfo(fileId:String,
                           name:String,
                           size:Long,
                           urlPrivate:String,
                           isPublic:Boolean,
                           mimeType:Option[String],
                           thumb_info:Option[(Int,String)]=None,
                           preview:Option[String]=None,
                           previewHighlight:Option[String]=None,
                           permalink:Option[String]=None,
                           permaLinkPublic:Option[String]=None,
                           title:Option[String]=None
                          )

  def slackFileInfoGen(js:JsValue): SlackFileInfo ={
    val slackInfo = SlackFileInfo(
      fileId = js \ "id",
      name = js \ "name",
      size = (js \ "size").as[Long],
      urlPrivate = js \ "url_private",
      isPublic = (js \ "is_public").as[Boolean],
      mimeType = js \ "mimetype",
      thumb_info = findThumbUrl(js),
      preview =  js \ "preview",
      previewHighlight = js \ "preview_highlight",
      permalink = js \ "permalink",
      permaLinkPublic = js \ "permalink_public",
      title = js \ "title"
    )
    slackInfo
  }
  implicit def jsLookupToOpt(value:JsLookupResult):Option[String] = value match{
    case(value:JsDefined) => Option(value.as[String])
    case _ => None
  }
  implicit def jsLookupToString(value:JsLookupResult):String = value match{
    case(value:JsDefined) =>value.as[String]
    case _ => ""
  }

  /**
    * Calls the sharedPublicUrl file Slack web-api method.
    * @param file SlackFileInfo object containing file metadata
    * @param slackSetup Slack setup object containing current token.
    * @return
    */
  def generatePublicUrl(file:SlackFileInfo)(implicit slackSetup: SlackSetup):Option[SlackFileInfo] = {
    val result = Http("http://slack.com/api/files.sharedPublicURL")
      .postData("{\"file\":\""+file.fileId+"}")
      .header("Content-Type","application/json; charset=utf-8")
      .header("Authorization",s"Bearer ${slackSetup.token}")
      .option(HttpOptions.readTimeout(10000)).asString
    if(result.is2xx) {
      Option(slackFileInfoGen(Json.parse(result.body)))
    }
    else{
      None
    }

  }

  /**
    * Checks if a file has a thumbnail with correct name exists on the download path
    * TODO:Include size check.
    * @param slackFileInfo Info about the file
    * @param slackSetup Settings provides path to check.
    * @return true if exists, false otherwise
    */
  def checkThumbDownload(slackFileInfo: SlackFileInfo)(implicit slackSetup: SlackSetup): Boolean ={
    if(!slackFileInfo.thumb_info.isDefined){
      true
    }
    else{
      val path =  getThumbPath(slackFileInfo)
      path.exists

    }
  }
  def checkFileDownloaded(slackFileInfo: SlackFileInfo)(implicit slackSetup: SlackSetup): Boolean ={
      val path =  getFilePath(slackFileInfo)
      path.exists && path.length == slackFileInfo.size

  }
  def getDownloadFileName(slackFileInfo: SlackFileInfo): String ={
    s"${slackFileInfo.fileId}-"+slackFileInfo.name.replaceAllLiterally(" ","_")
  }
  def getThumbPath(slackFileInfo: SlackFileInfo)(implicit slackSetup: SlackSetup): File ={
    getPathForFileName("thumbnail-"+getDownloadFileName(slackFileInfo))
  }
  def checkDownloaded(slackFileInfo: SlackFileInfo)(implicit slackSetup: SlackSetup): Boolean ={
    getFilePath(slackFileInfo).exists
  }
  def getFilePath(slackFileInfo: SlackFileInfo)(implicit slackSetup: SlackSetup): File ={
    getPathForFileName(getDownloadFileName(slackFileInfo))
  }
  def getPathForFileName(name:String)(implicit slackSetup: SlackSetup): File ={
    File(s"${slackSetup.assetDirectory}${File.separator}${blogPrefix}${File.separator}${name}")
  }
  def checkAndDownloadUrlWithName( url:String,fileName:String)(implicit slackSetup:SlackSetup):String={
    val file = getPathForFileName(fileName)
    if(!file.exists){
      val result = downloadSlackUrl(url)
      FileHandler.writeFile(result,file)
      getDownloadedFileUrlBase(fileName)
    }
    else{
    getDownloadedFileUrlBase(fileName)
    }
  }
  def getDownloadedFileUrl(slackFileInfo: SlackFileInfo): String ={
    getDownloadedFileUrlBase(getDownloadFileName(slackFileInfo))
  }
  def getDownloadedFileUrlBase(name: String): String ={

    s"/assets/${blogPrefix}/${name}"
  }
  def getDownloadedThumbUrl(slackFileInfo: SlackFileInfo): String ={
    getDownloadedFileUrlBase(s"thumbnail-${getDownloadFileName(slackFileInfo)}")
  }

  def downloadSlackUrl(url:String)(implicit slackSetup: SlackSetup):Option[Array[Byte]] = {
    val result = Http(url)
      .header("Authorization","Bearer "+ slackSetup.token)
      .options(HttpOptions.followRedirects(true))
      .option(HttpOptions.readTimeout(10000)).asBytes
    if(result.is2xx) {
      Option(result.body)
    }
    else{
      None
    }
  }
  def downloadThumb(file:SlackFileInfo)(implicit slackSetup: SlackSetup):Option[Array[Byte]] = {
    downloadSlackUrl(file.thumb_info.getOrElse((1,"NotAUrl"))._2)
  }
  def downloadFile(file:SlackFileInfo)(implicit slackSetup: SlackSetup):Option[Array[Byte]] = {
    downloadSlackUrl(file.urlPrivate)
  }

  def checkTextMessageForFiles(messageJson: JsValue): Option[List[SlackFileInfo]] = {
    val files = messageJson \ "files"
    files match {
      case filesJson: JsDefined =>
        filesJson.get match {
          case jsonValue:JsArray => {
            Option(jsonValue.as[List[JsValue]].map(jv => slackFileInfoGen(jv)))
            }
          case _ => None
        }
      case _ => None
    }
  }

  /**
    * Checks json raw message for info on uploaded files.
    *
    * @param messageJson <code>JsValue</code> from Slack web API
    * @return
    */
  def checkFiles(messageJson:JsValue): Option[List[SlackFileInfo]] ={
    val subType = messageJson \ "subtype"
    subType match {
      case subType:JsDefined =>
        checkFileSubtypeMessage(messageJson,subType)
      case _ =>
        checkTextMessageForFiles(messageJson)
    }
  }
  def checkFileSubtypeMessage(messageJson:JsValue,subType:JsDefined) = {

    if(subType.as[String] == FILE_TYPE){
      val file = messageJson \ "file"
      Option(List(slackFileInfoGen(file.get)))
    }
    else{
      None
    }
  }
  private def findThumbUrl(js:JsValue,maxSize:Int=800): Option[(Int,String)] ={
    val jso = js.asInstanceOf[JsObject]
    jso.fields.filter(ks => ks._1.matches("thumb_\\d+$"))
      .map(kj => (kj._1.split('_')(1).toInt,kj._2))
      .filter(ij => ij._1<=maxSize).sortBy(ij => ij._1)
      .lastOption
      .map(ij => (ij._1,ij._2.as[String]))

  }




}
