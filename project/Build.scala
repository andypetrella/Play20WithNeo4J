import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "Play20WithNeo4J"
    val appVersion      = "1.0"

	val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
	  
    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers ++= Seq(
        sbtIdeaRepo
      )
    )

}
