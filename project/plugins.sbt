addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.20")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
addSbtCoursier

libraryDependencies += "io.circe" %% "circe-core" % "0.11.1"
