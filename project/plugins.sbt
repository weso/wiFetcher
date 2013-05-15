logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

resolvers += "Templemore Repository" at "http://templemore.co.uk/repo/"

addSbtPlugin("templemore" % "sbt-cucumber-plugin" % "0.7.2")
