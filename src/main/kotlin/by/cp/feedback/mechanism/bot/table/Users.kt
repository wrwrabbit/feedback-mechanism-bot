package by.cp.feedback.mechanism.bot.table

import by.cp.feedback.mechanism.bot.model.UserStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object Users : LongIdTable(name = "users") {
    val langCode: Column<String> = text("lang_code")
    val status: Column<UserStatus> = enumerationByName("status", 255, UserStatus::class)
    val voteCount: Column<Long> = long("vote_count")
    val pollCount: Column<Long> = long("poll_count")
}
