package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.table.Polls
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

object PollRepository {

    private val db = DatabaseConfiguration.database

    fun save(userId: Long, question: String, options: Array<String>, allowMultipleAnswers: Boolean): PollDto =
        transaction {
            val createdAt = LocalDateTime.now(ZoneOffset.UTC)
            val id = Polls.insertAndGetId {
                it[Polls.userId] = userId
                it[Polls.question] = question
                it[Polls.options] = options
                it[Polls.allowMultipleAnswers] = allowMultipleAnswers
                it[Polls.createdAt] = createdAt
            }.value
            PollDto(id, userId, question, options, allowMultipleAnswers, createdAt)
        }

    fun getById(id: Long): PollDto? = transaction {
        Polls.select { Polls.id eq id }
            .map {
                PollDto(
                    it[Polls.id].value,
                    it[Polls.userId],
                    it[Polls.question],
                    it[Polls.options],
                    it[Polls.allowMultipleAnswers],
                    it[Polls.createdAt]
                )
            }.firstOrNull()
    }

    fun lastUserPoll(userId: Long): PollDto? = transaction {
        Polls.select { Polls.userId eq userId }
            .orderBy(Polls.createdAt to SortOrder.DESC)
            .limit(1)
            .map {
                PollDto(
                    it[Polls.id].value,
                    it[Polls.userId],
                    it[Polls.question],
                    it[Polls.options],
                    it[Polls.allowMultipleAnswers],
                    it[Polls.createdAt]
                )
            }.firstOrNull()
    }

}