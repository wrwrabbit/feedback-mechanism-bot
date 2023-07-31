package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UserRepository {

    fun save(userId: Long, langCode: String) = transaction {
        Users.insertAndGetId {
            it[Users.id] = userId
            it[Users.langCode] = langCode
            it[pollCount] = 0
            it[voteCount] = 0
        }.value
    }

    fun exists(userId: Long): Boolean = transaction {
        !Users.select { Users.id eq userId }.empty()
    }

    fun pollCountInc(userId: Long) = transaction {
        Users.update({ Users.id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(pollCount, pollCount + 1)
            }
        }
    }

    fun captchaRequired(userId: Long) = transaction {
        Users.select { Users.id eq userId }.map { it[Users.pollCount] to it[Users.voteCount] }.first()
            .let { it.first < 1 && it.second < 2 }
    }

    fun voteCountInc(userId: Long) = transaction {
        Users.update({ Users.id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(voteCount, voteCount + 1)
            }
        }
    }

}