import org.typelevel.scalacoptions.ScalacOptions

val scala3Version = "3.3.6"

val inCI = sys.env.get("CI").contains("true")

lazy val root = project
  .in(file("."))
  .settings(
    name := "tree",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalatest"     %% "scalatest"       % "3.2.19"   % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
      compilerPlugin(
        "org.polyvariant" % "better-tostring" % "0.3.17" cross CrossVersion.full
      )
    ),
    scalafmtOnCompile := !inCI,
    scalafixOnCompile := !inCI,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    tpolecatScalacOptions ++= Set(
      ScalacOptions.other("-java-output-version:17"),
      ScalacOptions.other("-source:future")
    ),
    Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )

addCommandAlias("commitCheck", "clean;test;scalafmtSbt;scalafmtAll;scalafixAll")
