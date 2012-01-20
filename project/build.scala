import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.patch"
  val buildScalaVersion = "2.9.1"
  val buildVersion      = "0.1.0-SNAPSHOT"

  val buildSettings = Defaults.defaultSettings ++
                      Seq (organization := buildOrganization,
                           scalaVersion := buildScalaVersion,
                           version      := buildVersion,
                           shellPrompt  := ShellPrompt.buildShellPrompt)
}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  val current = """\*\s+(\w+)""".r

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)

  val buildShellPrompt = {
    (state: State) => {
      val currBranch = current findFirstMatchIn gitBranches map (_ group(1)) getOrElse "-"
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (currProject, currBranch, BuildSettings.buildVersion)
    }
  }
}

object Resolvers {
  val jbossResolver = "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss"
  val repo1Resolver = "repo1" at "http://repo1.maven.org/maven2"
  val javaNetResolvers = "Java.net Maven 2 Repo" at "http://download.java.net/maven/2"
}

object Dependencies {
  val sprayCanVersion = "0.9.2-SNAPSHOT"
  val akkaVersion = "2.0-SNAPSHOT"
  val logbackVersion = "1.0.0"

  val sprayCan  = "cc.spray.can" % "spray-can" % sprayCanVersion
  val akkaActor = "com.typesafe.akka" % "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" % "akka-slf4j" % akkaVersion
  val slf4j     = "org.slf4j" % "slf4j-api" % "1.6.4"
  val logback   = "ch.qos.logback" % "logback-classic" % logbackVersion
}

object ArticleServiceBuild extends Build {
  val buildShellPrompt = ShellPrompt.buildShellPrompt

  import Dependencies._
  import BuildSettings._
  import Resolvers._

  val coreDeps = Seq(sprayCan,akkaActor,slf4j,akkaSlf4j,logback)

  lazy val sprayCanPong = Project("spray-can-pong",
                                  file("."),
                                  settings = buildSettings ++ Seq(scalacOptions ++= Seq("-deprecation","-optimise"),
                                                                  libraryDependencies := coreDeps,
                                                                  resolvers ++= Seq(jbossResolver,javaNetResolvers,repo1Resolver)))
}
