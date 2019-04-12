inThisBuild(List(
              organization := "se.etimo",
	            scalaVersion := "2.12.8",
              git.useGitDescribe := true,
            ))

name := "Etimo slack static blogger"

enablePlugins(JavaAppPackaging, GitVersioning)

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core_2.11" % "2.1.0"
libraryDependencies += "com.sksamuel.scrimage" % "scrimage-io-extra_2.11" % "2.1.0"
libraryDependencies += "com.sksamuel.scrimage" % "scrimage-filters_2.11" % "2.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % Test
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0" withJavadoc() withSources()
libraryDependencies += "com.vdurmont"% "emoji-java" % "4.0.0" withJavadoc() withSources()
libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2" withJavadoc() withSources()

run / fork := true

dockerRepository := Some("registry.kubernetes.etimo.se")

val dockerRegistryCertDir = settingKey[File]("Directory containing registry certificates and settings")
dockerRegistryCertDir := file("deploy/registry-keys")

// Skopeo supports client TLS without requiring daemon config changes
inConfig(Docker)(publish := {
  val _ = publishLocal.value
  val log = streams.value.log
  val registryCertOpts = Seq("--dest-cert-dir", dockerRegistryCertDir.value.toString)
  dockerAliases.value.foreach { tag =>
    val cmd = Seq("skopeo", "copy") ++ registryCertOpts ++ Seq(s"docker-daemon:$tag", s"docker://$tag")
    log.debug("Executing " + cmd.mkString(" "))
    val ret = sys.process.Process(cmd).! // publishLogger(log)
    if (ret != 0)
      sys.error("Nonzero exit value: " + ret)
    else
      log.info("Published image " + tag)
  }
})

val kubernetesHelmImageValues = taskKey[File]("A Helm values.yaml file containing all image references")
kubernetesHelmImageValues := {
  import _root_.io.circe.Json
  val values = Json.obj(
    "images" -> Json.obj(
      packageName.value.replace("-", "_") -> Json.fromString(dockerAlias.value.toString)
    )
  )
  val file = target.value / "helm-images.yaml"
  IO.write(file, values.spaces2)
  file
}
