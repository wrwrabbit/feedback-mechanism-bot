package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.UserDto
import by.cp.feedback.mechanism.bot.model.UserStatus
import by.cp.feedback.mechanism.bot.table.MessageQueue
import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    fun save(userId: Long, langCode: String) = transaction {
        Users.insertAndGetId {
            it[Users.id] = userId
            it[Users.langCode] = langCode
            it[status] = UserStatus.UNMUTED
            it[pollCount] = 0
            it[voteCount] = 0
        }.value
    }

    fun changeUserStatus(userId: Long, status: UserStatus) = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.status] = status
        }
    }

    fun exists(userId: Long): Boolean = transaction {
        !Users.select { Users.id eq userId }.empty()
    }

    fun getById(userId: Long) = transaction {
        Users.select { Users.id eq userId }.map {
            UserDto(
                id = it[Users.id].value,
                langCode = it[Users.langCode],
                status = it[Users.status],
            )
        }.firstOrNull()
    }

    fun pollCountInc(userId: Long) = transaction {
        Users.update({ Users.id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(pollCount, pollCount + 1)
            }
        }
    }

    fun captchaRequired(userId: Long) = transaction {
        Users.select { Users.id eq userId }.map { it[Users.pollCount] to it[Users.voteCount] }.firstOrNull()
            ?.let { it.first < 1 && it.second < 1 }
    }

    fun voteCountInc(userId: Long) = transaction {
        Users.update({ Users.id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(voteCount, voteCount + 1)
            }
        }
    }

    fun deleteDataByUserId(userId: Long) = transaction {
        Users.deleteWhere { Users.id eq userId }
        Polls.update({ Polls.userId eq userId }) {
            it[Polls.userId] = null
        }
        MessageQueue.deleteWhere { MessageQueue.userId eq userId }
    }

}