package name.devries.tslint

import sbt._
import sbt.Keys._
import sbt.File
import spray.json.{JsValue, JsString, JsObject}
import com.typesafe.sbt.jse.SbtJsTask
import com.typesafe.sbt.web.SbtWeb

object SbtTSLint extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  object autoImport {

    val tslint = TaskKey[Seq[File]]("tslint", "Perform Typescript linting.")

    val config = SettingKey[Option[File]]("tslint-config",
      "The location of a JSHint configuration file. Default is ./tslint.json")
    val formatter = SettingKey[Option[String]]("tslint-formatter", "The tslint formatter. Default is: prose")
    val formattersDirectory = SettingKey[Option[String]]("tslint-formatters-dir", "The directory for tslint formatters. Not required.")
    val rulesDirectories = SettingKey[Option[List[String]]]("tslint-rules-dirs",
      "The zero or more directories of rules directories. Default is empty list.")
    val resolvedConfig = TaskKey[Option[File]]("tslint-resolved-config",
      "The actual location of a tslint configuration file if present. If tslint-config is none then the task will seek a tslint.json in the project folder. If that's not found then tslint.json will be searched for in the user's home folder.")

    val tslintEslintRulesDir = SettingKey[String]("tslint-eslint-rules-dir",
      "The directory of the eslint extension of tslint rules if the tslint-eslint-rules npm webjar is added as a dependency to the build")
    val tslintMsContribRulesDir = SettingKey[String]("tslint-ms-contrib-rules-dir",
      "The directory of the tslint microsoft contrib rules if the tslint-microsoft-contrib npm webjar is added as a dependency to the build")
    val ng2LintRulesDir = SettingKey[String]("tslint-ng2-lint-rules-dir",
      "The directory of the angular2 lint rules if the codelyzer npm webjar is added as a dependency to the build")
  }

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport._

  val tslintUnscopedSettings = Seq(
    includeFilter in tslint := GlobFilter("*.ts"),
    resolvedConfig := {
      config.value.orElse {
        val tsLintConfig = "tslint.json"
        val projectRc = baseDirectory.value / tsLintConfig
        if (projectRc.exists()) {
          Some(projectRc)
        } else {
          val homeRc = file(System.getProperty("user.home")) / tsLintConfig
          if (homeRc.exists()) {
            Some(homeRc)
          } else {
            None
          }
        }
      }: Option[File]
    },
    jsOptions := createJsOptions(
      Map(
        "configFile" -> JsString(resolvedConfig.value.map((file) => { file.getAbsolutePath }).getOrElse("")),
        "formatter" -> JsString(formatter.value.getOrElse("prose"))
      ),
      formattersDirectory.value.map(JsString(_)),
      rulesDirectories.value.getOrElse(List.empty).map(JsString(_))
    ).toString


  )

  private[this] def createJsOptions(acc:Map[String, JsValue],formattersDir:Option[JsString],rulesDirs:List[JsString]):JsObject={
    val fdAsList=formattersDir match{
      case None =>  List.empty
      case Some(x) => List(x)
    }

    val extraOptions = List(fdAsList.map("formattersDirectory"-> _),
      rulesDirs.map("rulesDirectory"-> _)
    ).flatten.toMap

    JsObject(acc++extraOptions)
  }

  override def projectSettings = Seq(
    formattersDirectory := None,
    rulesDirectories := None,
    formatter := None,
    config := None,
    tslintEslintRulesDir := ((webJarsNodeModulesDirectory in Assets).value / "tslint-eslint-rules" / "dist" /"rules").getCanonicalPath,
    tslintMsContribRulesDir := ((webJarsNodeModulesDirectory in Assets).value / "tslint-microsoft-contrib").getCanonicalPath,
    ng2LintRulesDir := ((webJarsNodeModulesDirectory in Assets).value / "codelyzer").getCanonicalPath

  ) ++ inTask(tslint)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(tslintUnscopedSettings) ++
      inConfig(TestAssets)(tslintUnscopedSettings) ++
      Seq(
        moduleName := "tslint",
        shellFile := getClass.getClassLoader.getResource("tslint.js"),
        taskMessage in Assets := "Typescript linting",
        taskMessage in TestAssets := "Typescript test linting"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(tslint) ++ Seq(
    tslint in Assets := (tslint in Assets).dependsOn(webJarsNodeModules in Assets).value,
    tslint in TestAssets := (tslint in TestAssets).dependsOn(webJarsNodeModules in TestAssets).value
  )

}
