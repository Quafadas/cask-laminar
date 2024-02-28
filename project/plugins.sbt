addSbtPlugin("org.scala-js"                % "sbt-scalajs"              % "1.15.0")
addSbtPlugin("org.portable-scala"          % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("io.spray"                    % "sbt-revolver"             % "0.10.0")
addSbtPlugin("ch.epfl.scala"               % "sbt-scalajs-bundler"      % "0.21.1")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"            % "1.0.0-beta44")

addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.5.0")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager" % "1.9.16")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"        % "0.11.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"         % "0.6.4")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
