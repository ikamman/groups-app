import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion     = "2.5.26"
val akkaHttpVersion = "10.1.10"
val monocleVersion  = "2.0.0"

lazy val `groups-app` = project
  .in(file("."))
  .settings(multiJvmSettings: _*)
  .settings(
    organization := "com.example.groupsapp",
    scalaVersion := "2.12.8",
    scalacOptions in Compile ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlog-reflective-calls",
      "-Xlint"
    ),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    javaOptions in run ++= Seq(
      "-Xms128m",
      "-Xmx1024m",
      "-Djava.library.path=./target/native"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"          %% "akka-actor"              % akkaVersion,
      "com.typesafe.akka"          %% "akka-remote"             % akkaVersion,
      "com.typesafe.akka"          %% "akka-cluster"            % akkaVersion,
      "com.typesafe.akka"          %% "akka-cluster-metrics"    % akkaVersion,
      "com.typesafe.akka"          %% "akka-cluster-tools"      % akkaVersion,
      "com.typesafe.akka"          %% "akka-cluster-sharding"   % akkaVersion,
      "com.typesafe.akka"          %% "akka-multi-node-testkit" % akkaVersion,
      "com.typesafe.akka"          %% "akka-http"               % akkaHttpVersion,
      "com.typesafe.akka"          %% "akka-http-spray-json"    % akkaHttpVersion,
      "com.github.julien-truffaut" %% "monocle-core"            % monocleVersion,
      "com.github.julien-truffaut" %% "monocle-macro"           % monocleVersion,
      "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.2",
      "org.scalatest"              %% "scalatest"               % "3.0.7" % Test,
      "io.kamon"                   % "sigar-loader"             % "1.6.6-rev002",
      "com.h2database"             % "h2"                       % "1.4.199",
      "io.getquill"                %% "quill-jdbc"              % "3.4.10",
    ),
    fork in run := true,
    mainClass in (Compile, run) := Some("com.example.groupsapp.GroupsApp"),
    // disable parallel tests
    parallelExecution in Test := false,
    licenses := Seq(
      ("CC0", url("http://creativecommons.org/publicdomain/zero/1.0"))
    )
  )
  .configs(MultiJvm)
