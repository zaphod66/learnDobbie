package org.zaphod.firstdoobie

import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.effect.{IO, IOApp}
import doobie.util.transactor.Transactor
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.util.log.LogHandler

import java.time.LocalDateTime
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}

object Main extends IOApp.Simple {

  case class Student(id: Int, name: String)

  def getAllStudentNames: IO[List[String]] = {
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", // JDBC connector
      "jdbc:postgresql://localhost:5432/demo", // database URL
      "docker",
      "docker"
    )

    val query = sql"SELECT name FROM Students".queryWithLogHandler[String](LogHandler.jdkLogHandler)
    val action = query.to[List]

    action.transact(xa)
  }

  // Algebra
  trait Students[F[_]] {
    def findById(id: Int): F[Option[Student]]
    def findAll: F[List[Student]]
    def create(name: String): F[Int]
    def deleteByName(name: String): F[Int]
    def findStudentsByInitial(letter: Char): F[List[Student]]
  }

  private object Students {
    def make[F[_]: MonadCancelThrow](xa: Transactor[F]): Students[F] = new Students[F] {
      override def findById(id: Int): F[Option[Student]] =
        sql"SELECT id, name FROM Students WHERE id=$id".query[Student].option.transact(xa)
      //  sql"SELECT id, name FROM Students WHERE id=$id".queryWithLogHandler[Student](LogHandler.jdkLogHandler).option.transact(xa)

      override def findAll: F[List[Student]] =
        sql"SELECT id, name FROM Students".queryWithLogHandler[Student](LogHandler.jdkLogHandler).to[List].transact(xa)

      override def create(name: String): F[Int] =
        sql"INSERT INTO Students(name) VALUES ($name)".updateWithLogHandler(LogHandler.jdkLogHandler).withUniqueGeneratedKeys[Int]("id").transact(xa)

      override def deleteByName(name: String): F[Int] =
        sql"DELETE FROM Students WHERE name=$name".updateWithLogHandler(LogHandler.jdkLogHandler).run.transact(xa)

      override def findStudentsByInitial(letter: Char): F[List[Student]] = {
        val select = fr"SELECT id, name"
        val from   = fr"FROM Students"
        val where  = fr"WHERE left(name, 1) = ${letter.toString}"

        val stmt   = select ++ from ++ where
        val action = stmt.queryWithLogHandler[Student](LogHandler.jdkLogHandler).to[List]

        action.transact(xa)
      }
    }
  }

  val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ce <- ExecutionContexts.fixedThreadPool[IO](8)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver", // JDBC connector
      "jdbc:postgresql://localhost:5432/demo", // database URL
      "docker",
      "docker",
      ce
    )
  } yield xa

  private def runFor(io: IO[_], duration: FiniteDuration): IO[Unit] = {
    IO.race(io.foreverM, IO.sleep(duration)) *> IO.unit
  }

  private def repeat(io: IO[_], i: Int): IO[Unit] = {
    if (i <= 0) IO.unit else {
      io *> repeat(io, i - 1)
    }
  }

  override def run: IO[Unit] = {
    IO.println(s"${LocalDateTime.now()} First Doobie") *>
    getAllStudentNames.map(_.mkString(">", ", ", "<")).map(println) *>
    IO.println(s"${LocalDateTime.now()} Using postgresResource") *>
    postgresResource.use { xa =>
      val repo = Students.make[IO](xa)
      val createAndRead: IO[Option[Student]] = repo.create("Mona").flatMap { id => repo.findById(id) }

      repo.findAll.map(_.mkString(">", ", ", "<")).map(println) *>
      repeat(createAndRead.map(println), 4) *>
      repo.findAll.map(_.mkString(">", ", ", "<")).map(println) *>
      repo.findStudentsByInitial('M').map(_.mkString(">", ", ", "<")).map(println) *>
      IO.println(s"start ---------------------------------------") *>
      runFor(createAndRead, 5.seconds) *>
      IO.println(s"stop  ---------------------------------------") *>
      repo.deleteByName("Mona").map(println) *>
      repo.findStudentsByInitial('M').map(_.mkString(">", ", ", "<")).map(println) *>
      IO.unit
    }
  }
}
