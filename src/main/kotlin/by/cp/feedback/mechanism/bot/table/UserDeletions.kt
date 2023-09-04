package by.cp.feedback.mechanism.bot.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object UserDeletions : Table(name = "user_deletions") {
    val userId: Column<Long> = long("user_id")
    val createdAt: Column<LocalDateTime> = datetime("created_at")
}
