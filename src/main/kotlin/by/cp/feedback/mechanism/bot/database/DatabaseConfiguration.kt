package by.cp.feedback.mechanism.bot.database

import org.jetbrains.exposed.sql.Database

object DatabaseConfiguration {

    val database = Database.connect(
        "jdbc:pgsql://localhost:5434/postgres", driver = "com.impossibl.postgres.jdbc.PGDriver",
        user = "postgres", password = "password"
    )

}