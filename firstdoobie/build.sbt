lazy val CatsEffectVersion = "3.1.1"
lazy val Fs2Version        = "3.0.3"
lazy val Http4sVersion     = "0.23.12"
lazy val CirceVersion      = "0.14.1"
lazy val DoobieVersion     = "1.0.0-RC2"
lazy val H2Version         = "2.1.214"
lazy val FlywayVersion     = "9.2.0"
lazy val LogbackVersion    = "1.2.3"
lazy val ScalaTestVersion  = "3.2.15"
lazy val ScalaCheckVersion = "1.15.4"

lazy val root = (project in file("."))
  .settings(
    organization := "org.zaphod",
    name := "firstDoobie",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    scalacOptions := Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel"   %% "cats-effect"         % CatsEffectVersion,
      "co.fs2"          %% "fs2-core"            % Fs2Version,

      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-core"          % CirceVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,

      "com.h2database"  %  "h2"                  % H2Version,
      "org.flywaydb"    %  "flyway-core"         % FlywayVersion,
      "org.tpolecat"    %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"    %% "doobie-postgres"     % DoobieVersion,
      "org.tpolecat"    %% "doobie-h2"           % DoobieVersion,
      "org.tpolecat"    %% "doobie-hikari"       % DoobieVersion,      

      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,

      "org.scalatest"   %% "scalatest"           % ScalaTestVersion  % Test,
      "org.scalacheck"  %% "scalacheck"          % ScalaCheckVersion % Test,
      "org.tpolecat"    %% "doobie-scalatest"    % DoobieVersion % Test
    )
  )

