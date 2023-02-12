package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import by.cp.feedback.mechanism.bot.model.PollsModerationDto
import by.cp.feedback.mechanism.bot.table.PollsModeration
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PollModerationRepository {

    private val db = DatabaseConfiguration.database

    fun save(pollId: Long, telegramId: Long) = transaction {
        PollsModeration.insertAndGetId {
            it[PollsModeration.id] = pollId
            it[PollsModeration.telegramId] = telegramId
            it[PollsModeration.approves] = listOf<Long>().toTypedArray()
            it[PollsModeration.rejectionReason] = null
        }.value
    }

    fun getByTelegramId(telegramId: Long): PollsModerationDto? = transaction {
        PollsModeration.select { PollsModeration.telegramId eq telegramId }
            .map {
                PollsModerationDto(
                    it[PollsModeration.id].value,
                    it[PollsModeration.telegramId],
                    it[PollsModeration.approves],
                    it[PollsModeration.rejectionReason]
                )
            }.firstOrNull()
    }

    fun updateApprovesByTelegramId(telegramId: Long, approves: Array<Long>) = transaction {
        PollsModeration.update({ PollsModeration.telegramId eq telegramId }) {
            it[PollsModeration.approves] = approves
        }
    }

    fun updateRejectionReasonByTelegramId(telegramId: Long, rejectionReason: String) = transaction {
        PollsModeration.update({ PollsModeration.telegramId eq telegramId }) {
            it[PollsModeration.rejectionReason] = rejectionReason
        }
    }

}