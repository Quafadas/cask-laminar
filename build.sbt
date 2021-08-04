import org.scalajs.linker.interface.ModuleInitializer
import java.io.File

Global / onChangedBuildSource := ReloadOnSourceChanges
val V = new {    
  val Scala      = "2.13.6"
  val ScalaGroup = "2.13"
  val organiseImports  = "0.5.0"
}

scalaVersion := V.Scala

val Dependencies = new {

  lazy val frontend = Seq(
    libraryDependencies ++=      
        Seq("com.raquo" %%% "laminar" % "0.13.0")    
  )

  lazy val backend = Seq(
    libraryDependencies += "com.lihaoyi" %% "cask" % "0.7.11", // webserver  - https://github.com/com-lihaoyi/cask
    libraryDependencies += "io.getquill"%% "quill-jdbc"%"3.4.10", // DB lib - https://getquill.io
    libraryDependencies +="org.postgresql"%"postgresql" % "42.2.8", // Postgres driver, note the single %
    libraryDependencies += "org.ekrich" %% "sconfig" % "1.4.4", // config - https://github.com/ekrich/sconfig
    libraryDependencies += "com.lihaoyi" %% "requests" % "0.6.9" // simple http library   
  )

  lazy val shared = Def.settings(
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.4.0" // for parsing things
  )

  lazy val tests = Def.settings(
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.7.10" % Test
  )
}

inThisBuild(
  Seq(
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % V.organiseImports,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := V.ScalaGroup
  )
)




lazy val root =
  (project in file(".")).aggregate(todo, backend, shared.js, shared.jvm)

lazy val todo = (project in file("modules/frontend"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    Compile / npmDependencies += "vega-embed" -> "6.18.2",
    Compile / npmDependencies += "vega" -> "5.19.1",
    Compile / npmDependencies += "vega-lite" -> "4.17.0",
    Compile / npmDependencies += "vega-view" -> "5.10.1",

    //scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
     Compile / scalaJSModuleInitializers  += {
      ModuleInitializer.mainMethod("example.frontend.Todo", "main").withModuleID("todo")
    },
    Dependencies.frontend,
    Dependencies.tests,
    requireJsDomEnv := true,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    webpackDevServerPort := 3000,
    webpackDevServerExtraArgs := Seq("--inline")
  ) 
  .settings(commonBuildSettings)

lazy val backend = (project in file("modules/backend"))
  .dependsOn(shared.jvm)
  .settings(Dependencies.backend)
  .settings(Dependencies.tests)
  .settings(commonBuildSettings)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)  
  .settings(
    Test / fork := false,
    Universal / mappings += {
      val appJs = (todo / Compile / fullOptJS).value.data
      appJs -> ("lib/prod.js")
    },
    Universal  / javaOptions ++= Seq(
/*       "--port 8080",
      "--mode prod" */
    ),
     Docker / packageName := "laminar-cask",
     testFrameworks += new TestFramework("example.backend.WithDbFramework")
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/shared"))
  .jvmSettings(Dependencies.shared)
  .jsSettings(Dependencies.shared)
  .jsSettings(commonBuildSettings)
  .jvmSettings(commonBuildSettings)
  .jsConfigure { project => project.enablePlugins(ScalaJSBundlerPlugin) }

lazy val fastLinkCompileCopy = taskKey[Unit]("")

val assetPath = "/src/main/resources/assets"
val jsPath = s"$assetPath/js"

fastLinkCompileCopy := {
  val backendDir = baseDirectory.in(backend).value.getAbsolutePath
  val files = (webpack in (todo , Compile, fastOptJS)).value  
  files.foreach{f => 
    IO.copyFile(
      f.data,
      baseDirectory.in(backend).value / jsPath / f.data.name
    )
  }  
  IO.copyFile(
    new File(s"""$backendDir/$assetPath/html/Todo_dev.html"""),
    new File(s"""$backendDir/$assetPath/Todo.html""")
  )    
}

lazy val fullOptCompileCopy = taskKey[Unit]("")

fullOptCompileCopy := {
  val backendDir = baseDirectory.in(backend).value.getAbsolutePath
  val files = (webpack in (todo , Compile, fullOptJS)).value  
    files.foreach{f => 
    IO.copyFile(
      f.data,
      baseDirectory.in(backend).value / jsPath / f.data.name
    )
  }  
  IO.copyFile(
    new File(s"""$backendDir/$assetPath/html/Todo_prod.html"""),
    new File(s"""$backendDir/$assetPath/Todo.html""")
  )    
}

lazy val commonBuildSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion := V.Scala,  
  scalacOptions ++= Seq(
    "-Ywarn-unused"
  )
)

addCommandAlias("runDev", ";fastLinkCompileCopy; backend/reStart --mode dev")
addCommandAlias("runProd", ";fullOptCompileCopy; backend/reStart --mode prod")

val scalafixRules = Seq(  
  "DisableSyntax",
  "LeakingImplicitClassVal",
  "ProcedureSyntax",
  "NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
  "clean",
  "backend/compile",
  "backend/test",
  "todo/test",
  "todo/compile",
  "todo/fastOptJS/webpack",
  "todo/test",
  "scalafmtCheckAll",
  s"scalafix --check $scalafixRules"
).mkString(";")

val PrepareCICommands = Seq(
  s"compile:scalafix --rules $scalafixRules",
  s"test:scalafix --rules $scalafixRules",
  "test:scalafmtAll",
  "compile:scalafmtAll",
  "scalafmtSbt"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)
