name := """exbeacon-takasago-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-language:reflectiveCalls", "-language:postfixOps", "-language:implicitConversions")

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.jcenterRepo
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

pipelineStages := Seq(rjs, digest)

RjsKeys.mainModule := "main"

doc in Compile <<= target.map(_ / "none")

// コードフォーマットの機能をオフにします
//scalariformSettings

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play" %% "anorm" % "2.5.3",
  cache,
  ws,
  specs2 % Test,
  "joda-time" % "joda-time" % "2.9.2",
  "org.joda" % "joda-convert" % "1.8",
  //"com.enragedginger" %% "akka-quartz-scheduler" % "1.6.0-akka-2.4.x",
  "com.enragedginger" % "akka-quartz-scheduler_2.11" % "1.3.0-akka-2.3.x",

  "org.postgresql" % "postgresql" % "9.4.1212",
  "org.webjars" % "requirejs" % "2.3.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",	// Add bootstrap helpers and field constructors (http://adrianhurt.github.io/play-bootstrap/)
  "com.mohiva" %% "play-silhouette" % "4.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % "test",
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.iheart" %% "ficus" % "1.2.6",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  //"org.apache.poi" % "poi" % "3.13",
  //"org.apache.poi" % "poi-ooxml" % "3.13",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


fork in run := false
