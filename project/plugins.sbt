addSbtPlugin("org.scala-js"                % "sbt-scalajs"              % "1.7.1")
addSbtPlugin("org.portable-scala"          % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("io.spray"                    % "sbt-revolver"             % "0.9.1")
addSbtPlugin("ch.epfl.scala"               % "sbt-scalajs-bundler"      % "0.20.0")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"            % "1.0.0-beta38")

addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.4.3")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager" % "1.9.6")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"        % "0.9.30")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"         % "0.6.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
