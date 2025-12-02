import sbt.Keys._

logLevel := Level.Warn

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

val IthSbtRepo   = "https://artifactory.intouchhealth.io/artifactory/sbt"
val IthMavenRepo = "https://artifactory.intouchhealth.io/artifactory/maven"
val LivongoPluginsMirror =
  "https://artifactory.intouchhealth.io/artifactory/maven-lvg-replicated-plugins-release-local"
val IthArtifactoryHost     = "artifactory.intouchhealth.io"
val IthArtifactoryUsername = "ARTIFACTORY_USERNAME"
val IthArtifactoryPassword = "ARTIFACTORY_PASSWORD"

// Resolve scala-xml version conflict between sbt-scoverage (2.3.0) and older plugins (1.0.6)
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

resolvers ++=
  ivy("ITH SBT Plugins Repo", IthSbtRepo) ::
    ivy("Livongo Plugins Mirror", LivongoPluginsMirror) ::
    ("IthMavenRepo" at IthMavenRepo) ::
    ivy("IthMavenRepo", IthMavenRepo) ::
    ("Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/") ::
    Nil

credentials ++= {
  val userOpt = sys.env.get(IthArtifactoryUsername).filter(_.nonEmpty)
  val passOpt = sys.env.get(IthArtifactoryPassword).filter(_.nonEmpty)

  (userOpt, passOpt) match {
    case (Some(user), Some(password)) =>
      List(
        Credentials("Artifactory Realm", IthArtifactoryHost, user, password),
        Credentials("Artifactory Ivy Realm", IthArtifactoryHost, user, password)
      )
    case _ => Nil
  }
}

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
//addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
addSbtPlugin("com.github.gseitz" % "sbt-release"             % "1.0.10")
addSbtPlugin("com.eed3si9n"      % "sbt-unidoc"              % "0.4.1")
addSbtPlugin("com.updateimpact"  % "updateimpact-sbt-plugin" % "2.1.3")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"           % "2.4.0")
addSbtPlugin("livongo"          %% "sbt-build-plugins"       % "7.0.10")
addSbtPlugin("livongo"          %% "sbt-tgps-build"          % "7.0.10")

def createIthCredential(realm: String): Credentials = {
  val username =
    sys.env
      .get(IthArtifactoryUsername)
      .filter(!_.isBlank)
      .getOrElse {
        System.err.println(s"[WARN] $IthArtifactoryUsername not set. Cannot access ITH Artifactory.")
        ""
      }

  val password =
    sys.env
      .get(IthArtifactoryPassword)
      .filter(!_.isBlank)
      .getOrElse {
        System.err.println(s"[WARN] $IthArtifactoryPassword not set. Cannot access ITH Artifactory.")
        ""
      }

  Credentials(realm, IthArtifactoryHost, username, password)
}

def ivy(name: String, repoUrl: String) =
  Resolver.url(name, url(repoUrl))(Resolver.ivyStylePatterns)
