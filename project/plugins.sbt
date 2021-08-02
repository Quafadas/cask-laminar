addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.6.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")

addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.4.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"        % "0.9.29")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"         % "0.6.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
