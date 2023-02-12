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

    fun save(id: Long) = transaction {
        PollsModeration.insertAndGetId {
            it[PollsModeration.id] = id
        }.value
    }

    fun getById(id: Long): PollsModerationDto? = transaction {
        PollsModeration.select { PollsModeration.id eq id }
            .map {
                PollsModerationDto(
                    it[PollsModeration.id].value,
                    it[PollsModeration.approves],
                    it[PollsModeration.rejectionReason]
                )
            }.firstOrNull()
    }

    fun updateApproves(id: Long, approves: Array<Long>) = transaction {
        PollsModeration.update({ PollsModeration.id eq id }) {
            it[PollsModeration.approves] = approves
        }
    }

    fun updateRejectionReason(id: Long, rejectionReason: String) = transaction {
        PollsModeration.update({ PollsModeration.id eq id }) {
            it[PollsModeration.rejectionReason] = rejectionReason
        }
    }

}