name := "cv"

Global / cancelable := false

lazy val core = (project in file("modules/core"))
  .settings(Common.settings)

lazy val cormen = (project in file("modules/cormen"))
  .settings(Common.settings)
  .settings(
    libraryDependencies ++= Common.Dependencies.testing
  )

lazy val root = (project in file("."))
  .settings(Common.settings)
  .dependsOn(core)
  .aggregate(core)
