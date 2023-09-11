import sbt.Keys._
import sbt._

object Common {
  object Dependencies {
    val specs2Core = "org.specs2" %% "specs2-core" % "4.20.2" % Test
    val specs2Mock = "org.specs2" %% "specs2-mock" % "4.20.2" % Test

    val mockitoCore = "org.mockito" % "mockito-core" % "5.5.0" % Test

    val testing: Seq[ModuleID] = Seq(specs2Core, specs2Mock, mockitoCore)
  }


  lazy val scalafmtSettings: Seq[Def.Setting[Task[sbt.File]]] = {
    import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtConfig
    Seq(scalafmtConfig := appConfiguration.value.baseDirectory() / ".scalafmt.conf")
  }

  val baseSettings: Seq[Setting[_]] = Seq(
    organization := "zhoga",
    scalaVersion := "2.13.11",
    scalacOptions ++= Seq(
    ),
    javaOptions += "-Duser.language=en",
    javaOptions += "-Duser.country=US",
  ) ++ scalafmtSettings

  val settings: Seq[Setting[_]] = baseSettings
}
