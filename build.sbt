import Dependencies._

enablePlugins(JavaAppPackaging)
lazy val root = (project in file(".")).
settings(
		inThisBuild(List(
				organization := "se.etimo",
				scalaVersion := "2.12.3",
				version      := "0.1.0-SNAPSHOT"
				)),
		name := "Etimo slack static blogger",
		libraryDependencies += scalaTest % Test,
		libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2",
mainClass in Compile := Some("Main")

	)
fork in run := true

