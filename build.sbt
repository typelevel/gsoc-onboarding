val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "gsoc-onboarding",
    scalaJSUseMainModuleInitializer := true,
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= List(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.armanbilge" %%% "calico" % "0.2.3",
      "org.http4s" %%% "http4s-circe" % "0.23.33",
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",
      "org.http4s" %%% "http4s-dom" % "0.2.12"
    )
  )
