package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UserRepository {

    private val db = DatabaseConfiguration.database

    fun save(userId: Long, langCode: String) = transaction {
        Users.insertAndGetId {
            it[Users.id] = userId
            it[Users.langCode] = langCode
        }.value
    }

    fun updateLangCode(id: Long, langCode: String) = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.langCode] = langCode
        }
    }

    fun exists(userId: Long): Boolean = transaction {
        !Users.select { Users.id eq userId }.empty()
    }

    fun langCodeById(userId: Long): String = transaction {
        Users.select { Users.id eq userId }.map { it[Users.langCode] }.first()
    }

}