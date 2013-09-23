// Comment to get more information during initialization

logLevel := Level.Warn

// Use the Play sbt plugin for Play projects

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("play.version"))

resolvers += "Templemore Repository" at "http://templemore.co.uk/repo/"

addSbtPlugin("templemore" %% "sbt-cucumber-plugin" % "0.8.0")