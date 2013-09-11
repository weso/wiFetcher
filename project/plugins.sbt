// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/" 
 
//resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots" 
 
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.ivy2/local"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.3")