package by.cp.feedback.mechanism.bot.database

import org.jetbrains.exposed.sql.Database

object DatabaseConfiguration {

    val database = Database.connect(
        System.getenv("DB_HOST") + System.getenv("DB_DATABASE"), driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER"), password = System.getenv("DB_PASSWORD")
    )

}