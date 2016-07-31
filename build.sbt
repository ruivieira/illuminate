name := "illuminate"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1"

libraryDependencies += "org.pegdown" % "pegdown" % "1.6.0"

mainClass in (Compile, run) := Some("Illuminate")