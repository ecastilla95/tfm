organization := "ecastilla95"
name := "tfm"
version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= List("-feature","-deprecation", "-unchecked", "-Xlint")

libraryDependencies ++= Seq(
  "org.scalatest"   %% "scalatest"    % "3.0.5"   % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
  "org.apache.spark" %% "spark-core" % "2.4.0",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.34.1",
  "com.typesafe" % "config" % "1.3.4"
)
