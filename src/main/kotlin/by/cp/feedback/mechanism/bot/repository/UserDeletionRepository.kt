package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.table.UserDeletions
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

object UserDeletionRepository {

    fun save(userId: Long) = transaction {
        UserDeletions.insert {
            it[Polls.userId] = userId
            it[Polls.createdAt] = LocalDateTime.now(ZoneOffset.UTC)
        }
    }

    fun countDayAgo(userId: Long) = transaction {
        UserDeletions.select {
            (UserDeletions.userId eq userId) and
                    (UserDeletions.createdAt greaterEq LocalDateTime.now())
        }.count()
    }

}